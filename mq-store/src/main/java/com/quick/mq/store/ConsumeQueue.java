package com.quick.mq.store;

import lombok.extern.slf4j.Slf4j;

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
    mappedFileQueue = new MappedFileQueue(storePath ,fileSize);

  }

  public boolean load() {
    boolean result = mappedFileQueue.load();
    log.info("consumerQueue文件 加载 {}" ,result ? "成功" : "失败");
    return result;
  }
}
