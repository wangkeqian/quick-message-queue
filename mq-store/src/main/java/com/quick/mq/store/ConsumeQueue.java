package com.quick.mq.store;

import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

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
  private final String storePath;

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
    mappedFileQueue = new MappedFileQueue(storePath ,fileSize);
    FileUtil.createDirOK(storePath + "/" + topic);
  }

  public boolean load() {
    boolean result = mappedFileQueue.load();
    log.info("consumerQueue文件 加载 {}" ,result ? "成功" : "失败");
    return result;
  }

  public void putMessagePositionWrapper(
          long offset, //消息在commitLog的物理偏移量
          int size,  //消息大小
          long clOffset, //消息在CommitLog的排序号
          String topic
  ) {
    MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
    if (mappedFile == null){
      mappedFile = mappedFileQueue.getLastMappedFile(topic,0);
    }
    //限制最大容量20
    this.byteBufferIndex.position(0);
    this.byteBufferIndex.limit(BYTE_BUFFER_INDEX_MAX_SIZE);
    this.byteBufferIndex.putLong(offset);
    this.byteBufferIndex.putInt(size);
    // 消息在CommitLog的序号 * 20字节 = 该消息在ConsumerQueue的物理偏移量
    long cqOffset = clOffset * BYTE_BUFFER_INDEX_MAX_SIZE;
    //需要比较下当前文件的最新物理位置 =？ cqOffset

    boolean result = mappedFile.appendMessage(this.byteBufferIndex.array());
  }
}
