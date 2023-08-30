package com.quick.mq.store;

import com.quick.mq.common.exchange.NettyMessage;

public interface MessageStore {

  /**
   * 1.检查
   * @return
   */
  boolean load();

  void start();

  void shutdown();

  void destroy();

  void acceptMessage(NettyMessage message);
}
