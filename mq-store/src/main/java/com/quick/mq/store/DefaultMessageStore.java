package com.quick.mq.store;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.utils.FileUtil;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.store.utils.StorePathUtils;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认消息存储器
 *
 * @author wangkq
 * @date 2023/8/20
 */
@Slf4j
public class DefaultMessageStore implements MessageStore{

  private final MessageStoreConfig messageStoreConfig;
  private final BrokerConfig brokerConfig;
  private final CommitLog commitLog;
  private ConcurrentHashMap<String ,ConsumeQueue> topicConsumerQueueTable;

  public DefaultMessageStore(MessageStoreConfig messageStoreConfig, BrokerConfig brokerConfig) {
    this.messageStoreConfig = messageStoreConfig;
    this.brokerConfig = brokerConfig;
    commitLog = new CommitLog(this);
    this.topicConsumerQueueTable = new ConcurrentHashMap<>();
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

      if (result){
        //恢复ConsumerQueue和CommitLog有效信息
        this.recover();
      }


    }catch (Exception ex){
      log.error("load CommitLog and consumerQueue fail" ,ex);
      result = false;
    }



    return result;
  }

  private void recover() {
    this.recoverConsumerQueue();
    this.commitLog.recover();
  }

  private void recoverConsumerQueue() {
    int maxPhysicOffset = 0;
    this.topicConsumerQueueTable.forEach((k,v) ->{
        ConsumeQueue queue = v;
        queue.recover();
    });


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
                5 * 1024 * 1024,
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
      topicConsumerQueueTable.put(topic ,consumeQueue);
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

  @Override
  public void acceptMessage(NettyMessage message) {

    this.commitLog.asyncPutMessage(message);

    ConsumeQueue consumeQueue = findConsumerQueue(message.getTopic());
    if (consumeQueue != null){
      consumeQueue.putMessagePositionWrapper(
              message.getClQueueOffset(),
              message.getWarpSize(),
              0,
              message.getTopic()
      );
    }
  }

  private ConsumeQueue findConsumerQueue(String topic) {
    ConsumeQueue consumeQueue = null;
    ConsumeQueue queue = this.topicConsumerQueueTable.get(topic);
    if (null == queue){
      consumeQueue = new ConsumeQueue(
              0,
              topic,
              messageStoreConfig.getCommitLogSize(),
              messageStoreConfig.getStorePathConsumerQueue(),
              this
              );

      topicConsumerQueueTable.put(topic ,consumeQueue);
    }
    return consumeQueue;
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
