package com.quick.mq.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.quick.mq.broker.BrokerServer;
import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.common.exchange.*;
import com.quick.mq.nameserv.config.ServiceDiscovery;
import com.quick.mq.nameserv.config.NamesServConfig;
import com.quick.mq.nameserv.config.zk.ZookeeperDiscovery;
import com.quick.mq.store.DefaultMessageStore;
import com.quick.mq.store.MessageStore;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.common.config.NettyClientConfig;
import com.quick.mq.common.config.NettyServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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

  private ReentrantLock consumerLock = new ReentrantLock();

  protected final AtomicInteger consumerId = new AtomicInteger(1);

  private static final AtomicInteger CONSUMER_CLIENT_ID = new AtomicInteger(0);



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

  public boolean acceptMessage(Message message) {

    this.messageStore.acceptMessage(message);

    return false;
  }

  public Response heartbeat(Message message) {
    Response response = new Response();
    log.info("服务端 接收到心跳 {} ", message);
    ConsumerNode data = JSONObject.parseObject((String) message.getData(), ConsumerNode.class);
    List<ConsumerNode> nodes = serviceDiscovery.findAllConsumerByTopic(data.getGroup(), data.getTopic());
    response.setMsgId(message.getMsgId());
    boolean notifyConsumerRebalance = false;
    if (data.getClientId() == null){
      log.info("有新消費者接入");
      registerConsumer(data,nodes);
      response.setResult(JSONObject.toJSONString(data));
      notifyConsumerRebalance = true;
    }else {
      ConsumerNode n1 = nodes.stream().filter(node -> node.getClientId().equalsIgnoreCase(data.getClientId())).findFirst().orElse(null);
      if (n1 == null){
        response.setStatus(Response.BAD_REQUEST);
        response.setErrorMsg("NOT FOUND CLINET ID = [" + data.getClientId() + "]");
        return response;
      }else {
        if (compareTo(data,n1)){
          notifyConsumerRebalance = true;
        }
      }
    }

    //通知某个consumer进行重平衡
    if (notifyConsumerRebalance){

    }

    return response;
  }

  private void registerConsumer(ConsumerNode data, List<ConsumerNode> nodes) {
    try {
      if (consumerLock.tryLock(10_000L, TimeUnit.MILLISECONDS)){
        if (ObjectUtil.isEmpty(nodes)){
          nodes = new ArrayList<>();
        }
        int clientId = CONSUMER_CLIENT_ID.addAndGet(1);
        data.setClientId(String.valueOf(clientId));
        nodes.add(data);
        serviceDiscovery.registerConsumer(data);
      }
    }catch (InterruptedException ie){
      log.error("消费端写入zk异常",ie);
      throw new RuntimeException(ie);
    }finally {
      consumerLock.unlock();
    }

  }

  private boolean compareTo(ConsumerNode data, ConsumerNode n1) {
    return false;
  }

  public Response prePull(Message message) {
    PullMessageRequest request = JSONObject.parseObject((String) message.getData(), PullMessageRequest.class);
    Map<String, Long> l = messageStore.queryEnableMessage(request);
    Response response = new Response();
    response.setResult(JSONObject.toJSONString(l));
    return response;
  }

  public Response Pull(Message message) {
    PullRequest pullRequest = JSONObject.parseObject((String) message.getData(), PullRequest.class);
    List<CommitLogMessage> messageList = messageStore.getMessage(pullRequest);
    Response response = new Response();
    String jsonString = JSONObject.toJSONString(messageList);
    response.setResult(jsonString);
    return response;
  }
}
