package com.quick.mq.store;

import com.quick.mq.common.exchange.CommitLogMessage;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.PullMessageRequest;
import com.quick.mq.common.exchange.PullRequest;

import java.util.List;
import java.util.Map;

public interface MessageStore {

  /**
   * 1.检查
   * @return
   */
  boolean load();

  void start();

  void shutdown();

  void destroy();

  void acceptMessage(Message message);

  Map<String, Long> queryEnableMessage(PullMessageRequest request);

  List<CommitLogMessage> getMessage(PullRequest pullRequest);
}
