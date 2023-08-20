package com.quick.mq.controller;

import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.store.DefaultMessageStore;
import com.quick.mq.store.MessageStore;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.config.NettyClientConfig;
import com.quick.mq.config.NettyServerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/19
 */
@Slf4j
public class BrokerController {
  private final BrokerConfig brokerConfig;
  private final MessageStoreConfig messageStoreConfig;
  private final NettyServerConfig nettyServerConfig;
  private final NettyClientConfig nettyClientConfig;
  private MessageStore messageStore;

  public BrokerController(BrokerConfig brokerConfig, MessageStoreConfig messageStoreConfig,
      NettyServerConfig nettyServerConfig, NettyClientConfig nettyClientConfig) {
    this.brokerConfig = brokerConfig;
    this.messageStoreConfig = messageStoreConfig;
    this.nettyServerConfig = nettyServerConfig;
    this.nettyClientConfig = nettyClientConfig;
  }





  public BrokerConfig getBrokerConfig() {
    return brokerConfig;
  }

  public MessageStoreConfig getMessageStoreConfig() {
    return messageStoreConfig;
  }

  public NettyServerConfig getNettyServerConfig() {
    return nettyServerConfig;
  }

  public NettyClientConfig getNettyClientConfig() {
    return nettyClientConfig;
  }

  public boolean init() {

    boolean result = true;
    try {
      this.messageStore = new DefaultMessageStore(messageStoreConfig, brokerConfig);

    }catch (Exception ex){
      log.error("初始化消息文件处理器 失败",ex);
      result = false;
    }
    // 初始化 消息文件存储相关
    result = result && messageStore.load();

    return false;
  }

  public void shutdown() {

  }
}
