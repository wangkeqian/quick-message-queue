package com.quick.mq.store;

public interface MessageStore {

  /**
   * 1.检查
   * @return
   */
  boolean load();

  void start();

  void shutdown();

  void destroy();

}
