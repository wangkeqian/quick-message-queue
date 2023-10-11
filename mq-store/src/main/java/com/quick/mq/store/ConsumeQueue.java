package com.quick.mq.store;

import com.quick.mq.common.exchange.ConsumerQueueMessage;
import com.quick.mq.common.utils.CountDownLatch2;
import com.quick.mq.common.utils.FileUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/20
 */
@Slf4j
public class ConsumeQueue {

  private final int queueId;
  private final String topic;
  private final int fileSize;
  private final DefaultMessageStore defaultMessageStore;
  private final MappedFileQueue mappedFileQueue;
  private final ByteBuffer byteBufferIndex;
  private final int BYTE_BUFFER_INDEX_MAX_SIZE = 20;
  protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);
  protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
  private final String storePath;
  //最大消息物理偏移量
  private long maxPhysicOffset = 0;
  //最大消息偏移量
  private long maxCqOffset = 0;
  //最大被消费偏移量
  private long maxConsumedCqOffset = -1;

  private final Lock lock = new ReentrantLock();

  public ConsumeQueue(
      int queueId,
      String topic,
      int fileSize,
      String storePath,
      DefaultMessageStore defaultMessageStore) {
    this.queueId = queueId;
    this.topic = topic;
    this.defaultMessageStore = defaultMessageStore;
    this.fileSize = fileSize;
    this.storePath = storePath;
    //20字节是每个消息在ConsumerQueue的大小
    this.byteBufferIndex = ByteBuffer.allocate(BYTE_BUFFER_INDEX_MAX_SIZE);

    String queueDir = this.storePath
        + File.separator + topic;

    mappedFileQueue = new MappedFileQueue(queueDir ,fileSize);
    FileUtil.createDirOK(storePath + "/" + topic);
  }

  public long getMaxPhysicOffset() {
    return maxPhysicOffset;
  }

  public long getMaxCqOffset() {
    return maxCqOffset;
  }

  public boolean load() {
    boolean result = mappedFileQueue.load();
    log.info("consumerQueue文件 加载 {}" ,result ? "成功" : "失败");
    return result;
  }
  protected void waitForRunning(long interval) {
    if (hasNotified.compareAndSet(true, false)) {
      return;
    }

    //entry to wait
    waitPoint.reset();

    try {
      waitPoint.await(interval, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.error("Interrupted", e);
    } finally {
      hasNotified.set(false);
    }
  }
  public void wakeup() {
    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }
  }
  public void putMessagePositionWrapper(
          long offset, //消息在commitLog的物理偏移量
          int size,  //消息大小
          long clOffset, //消息在CommitLog的排序号
          String topic
  ) {
    MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
    if (mappedFile == null){
      mappedFile = mappedFileQueue.getLastMappedFile(0);
    }
    //限制最大容量20
    this.byteBufferIndex.position(0);
    this.byteBufferIndex.limit(BYTE_BUFFER_INDEX_MAX_SIZE);
    this.byteBufferIndex.putLong(offset);
    this.byteBufferIndex.putInt(size);
    //是否被消费
    this.byteBufferIndex.putInt(0);
    //预留 现在用不到
    this.byteBufferIndex.putInt(1);
    // 消息在CommitLog的序号 * 20字节 = 该消息在ConsumerQueue的物理偏移量
    long cqOffset = clOffset * BYTE_BUFFER_INDEX_MAX_SIZE;
    //需要比较下当前文件的最新物理位置 =？ cqOffset

    boolean result = mappedFile.appendMessage(this.byteBufferIndex.array());
    this.maxCqOffset ++;
    //释放
    wakeup();
  }

  public void recover() {
    MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
    if (mappedFile == null){
      return;
    }
    ByteBuffer buffer = mappedFile.getSliceByteBuffer();
    //ConsumerQueue队列的消息偏移量

    for (int i = 0; i < fileSize; i += BYTE_BUFFER_INDEX_MAX_SIZE) {
      //消息在commitLog的物理偏移量
      long offset = buffer.getLong();
      //消息总大小
      int size = buffer.getInt();
      int hasConsumed = buffer.getInt();
      //当前被消费节点最大节点
      if (this.maxConsumedCqOffset == -1 && hasConsumed == 1){
        this.maxConsumedCqOffset = i;
      }
      //预留
      long def = buffer.getInt();
      if (offset >=0 && size > 0){
        //最后maxPhysicOffset = 最后一条消息的物理偏移量 + 长度
        this.maxPhysicOffset += BYTE_BUFFER_INDEX_MAX_SIZE;
      }
      if (size > 0){
        //消息最大偏移量
        this.maxCqOffset += 1;
      }
    }
    //没有被消费过
    if (this.maxConsumedCqOffset == -1){
      this.maxConsumedCqOffset = 0;
    }
    mappedFile.setWrotePosition(Math.toIntExact(maxPhysicOffset));
    log.info("消息总数： {} ，等待消费偏移量位置 ：{}" ,maxCqOffset ,maxConsumedCqOffset);
  }

  public List<ConsumerQueueMessage> captureMessage(Integer cqStartOffset, Integer cqEndOffset){
    MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
    if (mappedFile == null){
      return null;
    }
    ByteBuffer buffer = mappedFile.getSliceByteBuffer();
    buffer.position(cqStartOffset * BYTE_BUFFER_INDEX_MAX_SIZE);
    log.info("小伙子这次想拉 {} ~ {}" ,cqStartOffset, cqEndOffset);
    int newOffset = 0;
    List<ConsumerQueueMessage> messages = new ArrayList<>();
    for (int i = cqStartOffset; i <= cqEndOffset; i++){
      //消息在commitLog的物理偏移量
      long offset = buffer.getLong();
      //消息总大小
      int size = buffer.getInt();
      int hasConsumed = buffer.getInt();
      //预留
      long def = buffer.getInt();
      ConsumerQueueMessage message = new ConsumerQueueMessage(offset, size, hasConsumed, def);
      messages.add(message);
      newOffset += BYTE_BUFFER_INDEX_MAX_SIZE;
    }
    log.info("这次拉取的起始位置={} 结束位置={}",cqStartOffset * BYTE_BUFFER_INDEX_MAX_SIZE ,cqStartOffset * BYTE_BUFFER_INDEX_MAX_SIZE + newOffset);
    return messages;
  }

  public Map getEnableConsumedOffset(){
    try{
      if (lock.tryLock(10, TimeUnit.SECONDS)){
        if (maxCqOffset > maxConsumedCqOffset){
          return quickReturn();
        }else {
          return requestHold();
        }
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return null;
  }

  private Map<String, Long> requestHold() {
//    log.info("开始等啊等啊等");
    waitForRunning(15_000);
//    log.info("终于释放了");
    if (maxCqOffset > maxConsumedCqOffset) {
      return quickReturn();
    }
    return null;
  }

  private Map<String, Long> quickReturn() {
    long res = maxConsumedCqOffset;
    if ( maxCqOffset - maxConsumedCqOffset > 32){
      maxConsumedCqOffset += 32;
    }else {
      maxConsumedCqOffset = maxCqOffset;
    }
    HashMap<String, Long> map = new HashMap<>();
    map.put("startOffset",res);
    map.put("endOffset",maxConsumedCqOffset - 1);
    return map;
  }
}
