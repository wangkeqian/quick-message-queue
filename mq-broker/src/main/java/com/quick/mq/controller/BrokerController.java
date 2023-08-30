package com.quick.mq.controller;

import com.quick.mq.broker.BrokerServer;
import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.nameserv.config.ServiceDiscovery;
import com.quick.mq.nameserv.config.NamesServConfig;
import com.quick.mq.nameserv.config.zk.ZookeeperDiscovery;
import com.quick.mq.store.DefaultMessageStore;
import com.quick.mq.store.MessageStore;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.common.config.NettyClientConfig;
import com.quick.mq.common.config.NettyServerConfig;
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
  private final NamesServConfig namesServConfig;
  private MessageStore messageStore;
  private BrokerServer brokerServer;
  private ServiceDiscovery serviceDiscovery;


  public BrokerController(BrokerConfig brokerConfig, MessageStoreConfig messageStoreConfig,
                          NettyServerConfig nettyServerConfig, NettyClientConfig nettyClientConfig, NamesServConfig namesServConfig) {
    this.brokerConfig = brokerConfig;
    this.messageStoreConfig = messageStoreConfig;
    this.nettyServerConfig = nettyServerConfig;
    this.nettyClientConfig = nettyClientConfig;
    this.namesServConfig = namesServConfig;
    this.brokerServer = new BrokerServer(nettyServerConfig ,nettyClientConfig,this);
    this.serviceDiscovery = new ZookeeperDiscovery(namesServConfig ,nettyServerConfig);
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

  /**
   * Broker控制器的启动会执行以下步骤
   * >初始化CommitLog对象
   *   CommitLog对象里初始化存储，服务，系统参数等配置
   * >CommitLog对象的实例化
   *   通过内存映射手段，MappedFile直接指向对应的存储文件
   *
   * >ConsumerQueue的实例化
   *   通过内存映射手段，MappedFile直接指向对应的存储文件
   * >IndexFile对象初始化
   *
   * >检测上次系统是否异常关闭
   *
   *
   * >netty 配置的初始化
   *
   * 定时任务启动
   * @return
   */
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

    return result;
  }



  public void start() {

    messageStore.start();

    //注册到服务中心
    serviceDiscovery.register();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      serviceDiscovery.removeServ();
    }));

    brokerServer.start();

  }


  public void shutdown() {

  }

  public boolean acceptMessage(NettyMessage message) {

    this.messageStore.acceptMessage(message);

    return false;
  }
}
