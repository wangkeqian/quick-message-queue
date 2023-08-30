package com.quick.mq.store;

import com.quick.mq.common.exchange.NettyMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 映射真实消息文件对象
 *
 * @author wangkq
 * @date 2023/8/20
 */
public class MappedFile {
  //Linux page cache （页缓存）size 默认 4k
  public static final int OS_PAGE_SIZE = 1024 * 4;
  private final String fileName;
  private final int mappedFileSize;
  private File file;
  protected FileChannel fileChannel;
  private MappedByteBuffer mappedByteBuffer;
  protected final AtomicInteger wrotePosition = new AtomicInteger(0);
  protected final AtomicInteger committedPosition = new AtomicInteger(0);
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

  public void sendMessage(final NettyMessage message) {
    int currentPos = this.wrotePosition.get();
    if (currentPos < mappedFileSize){

      ByteBuffer byteBuffer = mappedByteBuffer.slice();
      byteBuffer.position(currentPos);





    }

  }
}
