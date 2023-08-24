package com.quick.mq.store;

import cn.hutool.core.lang.Assert;
import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.common.utils.FileUtil;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.store.utils.StorePathUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认消息存储器
 *
 * @author wangkq
 * @date 2023/8/20
 */
public class DefaultMessageStore implements MessageStore{

  private final MessageStoreConfig messageStoreConfig;
  private final BrokerConfig brokerConfig;
  private final CommitLog commitLog;
  private ConcurrentHashMap<String ,HashMap<Integer, ConsumeQueue>> topicConsumerQueueTable;

  public DefaultMessageStore(MessageStoreConfig messageStoreConfig, BrokerConfig brokerConfig) {
    this.messageStoreConfig = messageStoreConfig;
    this.brokerConfig = brokerConfig;
    commitLog = new CommitLog(this);

    //初始化commitLog,consumerQueue文件夹
    FileUtil.createDirOK(getCommitLogPath());
    FileUtil.createDirOK(getConsumerQueuePath());
  }

  private String getConsumerQueuePath() {
    Assert.notBlank(messageStoreConfig.getStorePathCommitLog() ,"请指定CommitLog路径");
    return messageStoreConfig.getStorePathCommitLog();
  }

  private String getCommitLogPath() {
    Assert.notBlank(messageStoreConfig.getStorePathConsumerQueue() ,"请指定consumerQueue路径");
    return messageStoreConfig.getStorePathConsumerQueue();
  }

  /**
   * 1.检查上次是否正常关停程序
   * 2.加载commitLog信息到内存
   * 3.加载consumerQueue信息到内存
   * @return
   */
  public boolean load() {
    boolean result = true;
    try {
      //判断最后一次程序是否正常停止，异常关闭会存在临时文件
      boolean lastShutdownOk = !this.isExistTmpFile();
      // CommitLog 加载
      result = this.commitLog.load();

      result = result && this.loadConsumerQueue();

    }catch (Exception ex){
      result = false;
    }



    return result;
  }

  private boolean loadConsumerQueue() {
    File consumerQueueDir = new File(this.messageStoreConfig.getStorePathConsumerQueue());
    File[] files = consumerQueueDir.listFiles();

    if (files != null){
      for (File topicFile : files) {
        //consumerQueue用消息 topic进行分类
        String topic = topicFile.getName();

        File[] consumerQueueFiles = topicFile.listFiles();
        if (consumerQueueFiles != null){
          for (File consumerQueue : consumerQueueFiles) {
            int queueId = Integer.parseInt(consumerQueue.getName());

            ConsumeQueue consumeQueue = new ConsumeQueue(
                queueId,
                topic,
                300_000,
                getMessageStoreConfig().getStorePathConsumerQueue(),
                this
            );
            //初始topic -> （queueId & consumerQueue） 映射关系
            this.setMappedRelation(topic ,queueId ,consumeQueue);
            if (!consumeQueue.load()){
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  private void setMappedRelation(String topic, int queueId, ConsumeQueue consumeQueue) {
    if (topicConsumerQueueTable == null){
      topicConsumerQueueTable = new ConcurrentHashMap<>(16);
      HashMap<Integer, ConsumeQueue> queue = new HashMap<>();
      queue.put(queueId, consumeQueue);
      topicConsumerQueueTable.put(topic ,queue);
    }else {
      HashMap<Integer, ConsumeQueue> queue = topicConsumerQueueTable.get(topic);
      if (queue == null){
        HashMap<Integer, ConsumeQueue> map = new HashMap<>();
        map.put(queueId, consumeQueue);
        topicConsumerQueueTable.put(topic ,map);
      }
    }
  }

  private boolean isExistTmpFile() {
    String filePath = StorePathUtils.getAbnormalFilePath(this.messageStoreConfig.getStoreRootPathDir());
    File file = new File(filePath);
    return file.exists();
  }

  public void start() {

  }

  public void shutdown() {

  }

  public void destroy() {

  }

  public MessageStoreConfig getMessageStoreConfig() {
    return messageStoreConfig;
  }

  public BrokerConfig getBrokerConfig() {
    return brokerConfig;
  }

  public CommitLog getCommitLog() {
    return commitLog;
  }
}
