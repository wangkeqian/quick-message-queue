package com.quick.mq.store.config;

import lombok.Data;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/19
 */
@Data
public class MessageStoreConfig {


  private String storeRootPathDir;
  // 消息储存文件commitLog文件路径
  private String storePathCommitLog;

  // 消息检索文件consumerQueue文件路径
  private String storePathConsumerQueue;

  // 消息索引文件indexFile文件路径
  private String storePathIndexFile;
  /**
   * commitLog 容量 默认 1G
   */
  private final int commitLogSize = 1024 * 1024 * 1024;

}
