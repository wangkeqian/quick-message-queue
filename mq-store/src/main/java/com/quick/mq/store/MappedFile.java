package com.quick.mq.store;

import com.quick.mq.common.exchange.Message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 映射真实消息文件对象
 *
 * @author wangkq
 * @date 2023/8/20
 */
@Slf4j
public class MappedFile {
  //Linux page cache （页缓存）size 默认 4k
  public static final int OS_PAGE_SIZE = 1024 * 4;
  private final String fileName;
  private final int mappedFileSize;
  private File file;
  protected FileChannel fileChannel;
  private MappedByteBuffer mappedByteBuffer;
  //当前写指针偏移量
  protected final AtomicInteger wrotePosition = new AtomicInteger(0);
  //当前提交偏移量
  protected final AtomicInteger committedPosition = new AtomicInteger(0);
  //当前刷盘偏移量
  private final AtomicInteger flushedPosition = new AtomicInteger(0);

  public MappedFile(final String fileName, final int mappedFileSize) throws IOException {
    this.fileName = fileName;
    this.mappedFileSize = mappedFileSize;
    init();
  }

  private void init() throws IOException {
    this.file = new File(fileName);
    this.fileChannel = new RandomAccessFile(fileName ,"rw").getChannel();
    //内存映射类，不需要把文件拷贝到用户JVM空间内存，从直接内存里读取文件
    this.mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, mappedFileSize);


  }


  public AtomicInteger getWrotePosition() {
    return wrotePosition;
  }
  public void setWrotePosition(int pos) {
    this.wrotePosition.set(pos);
  }

  public AtomicInteger getCommittedPosition() {
    return committedPosition;
  }
  public void setCommittedPosition(int pos) {
    this.committedPosition.set(pos);
  }

  public AtomicInteger getFlushedPosition() {
    return flushedPosition;
  }

  public void setFlushedPosition(int pos) {
    this.flushedPosition.set(pos);
  }

  public ByteBuffer getSliceByteBuffer(){
    return this.mappedByteBuffer.slice();
  }

  public static void main(String[] args) {
    String str = "你好,wkq";
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    int length = bytes.length;
    ByteBuffer wrap = ByteBuffer.allocate(4 + 8 + 4 + length);
    wrap.limit(wrap.capacity());
    wrap.position(0);
    wrap.putInt(length);
    wrap.putLong(System.currentTimeMillis());
    wrap.putInt(100);
    wrap.put(bytes);

    wrap.flip();
    int lg = wrap.getInt();
    long aLong = wrap.getLong();
    int anInt1 = wrap.getInt();
    byte[] data = new byte[lg];
    ByteBuffer byteBuffer = wrap.get(data);
    String string = new String(data);
    System.out.println(string);

  }
  public void sendMessage(final Message message) {
    String originalData = (String) message.getData();
    byte[] bytes = originalData.getBytes(StandardCharsets.UTF_8);
    final int dataLength = bytes.length;

    ByteBuffer wrap = ByteBuffer.allocate(4 + 8 + 4 + dataLength);
    wrap.position(0);
    wrap.limit(wrap.capacity());
    //消息体数据长度
    wrap.putInt(dataLength);
    //消息存储时间
    wrap.putLong(System.currentTimeMillis());
    //消息在消息队列偏移量 todo

    //消息在CommitLog的偏移量
    int currentPos = this.wrotePosition.get();
    log.info("消息{} 当前写位置 {}" ,originalData ,currentPos);
    wrap.putInt(currentPos);
    //消息体
    wrap.put(bytes);

    if (currentPos < mappedFileSize){
      ByteBuffer byteBuffer = mappedByteBuffer.slice();
      byteBuffer.position(currentPos);
      byteBuffer.put(wrap.array());
      this.mappedByteBuffer.force();
      int i = this.wrotePosition.addAndGet(wrap.capacity());
      log.info("消息{} 下个消息写位置 {}" ,originalData ,i);
      this.committedPosition.addAndGet(1);
    }
    {
      message.setClQueueOffset(currentPos);
      message.setWarpSize(wrap.capacity());
    }
  }

  public boolean appendMessage(byte[] data) {
    int currentPos = this.wrotePosition.get();

    if (currentPos + data.length <= mappedFileSize){
      ByteBuffer buffer = this.getSliceByteBuffer();
      log.info("这次存储的位置={}",currentPos);
      buffer.position(currentPos);
      buffer.put(data);
      this.mappedByteBuffer.force();

      this.wrotePosition.addAndGet(data.length);
      this.committedPosition.addAndGet(1);
      return true;
    }
    return false;
  }
}
