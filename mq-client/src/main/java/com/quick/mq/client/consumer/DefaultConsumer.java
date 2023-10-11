package com.quick.mq.client.consumer;

import ch.qos.logback.classic.spi.STEUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quick.mq.client.task.ConsumerHeartBeatService;
import com.quick.mq.client.task.ConsumerPushMessageService;
import com.quick.mq.common.exchange.CommitLogMessage;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.PullRequest;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class DefaultConsumer implements Consumer{
    private final String consumerGroup;
    private String topic;
    private Integer queueId;
    private String nameServ;
    private final RebalanceImpl rebalanceImpl;
    private ConsumerHeartBeatService heartBeatService;
    private final ConsumerPushMessageService pushMessageService;
    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();

    private long consumedOffset;

    private final NettyClient client;
    public DefaultConsumer() {
        this("default_group");
    }

    public DefaultConsumer(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        rebalanceImpl = new RebalancePushImpl(this);
        client = new NettyClient();
        pushMessageService = new ConsumerPushMessageService();
        init();
    }

    public void subscribe(String topic) {
        this.topic = topic;
        heartBeatService = new ConsumerHeartBeatService(consumerGroup ,topic);
    }

    public void setNameServ(String nameServ) {
        this.nameServ = nameServ;
    }

    private void init() {

    }

    public void start() throws InterruptedException {
        boolean result = false;
        //通知broker，本consumer启动了
        heartBeatService.start();
        pushMessageService.start(topic, 1 ,this);

        while (true){
            PullRequest take = pullRequestQueue.take();
            List<CommitLogMessage> message = pullMessage(take);
            for (CommitLogMessage commitLogMessage : message) {
                log.info("拉到了 数据是 {}",new String(commitLogMessage.getBytesBody(), StandardCharsets.UTF_8));
            }
        }
    }

    public void addPullRequestQueue(PullRequest pullRequest){
        this.pullRequestQueue.add(pullRequest);
    }

    private List<CommitLogMessage> pullMessage(PullRequest take) {
//        log.info("终于拉到了 起始偏移量 ：{}，结束偏移量 ：{}" ,take.getCqStartOffset() ,take.getCqEndOffset());
        NettyClient client = new NettyClient();
        Message message = new Message(null , JSONObject.toJSONString(take), MessageType.CONSUMER_PUll_REQUEST);
        List<CommitLogMessage> data = new ArrayList<>();
        try {
            Response send = client.send(message);
            if (send.getStatus() == Response.OK){
                String result = (String) send.getResult();

                List list = JSONObject.parseObject(result, List.class);
                for (Object o : list) {
                    CommitLogMessage commitLogMessage = JSONObject.parseObject(String.valueOf((JSONObject) o), CommitLogMessage.class);
                    data.add(commitLogMessage);
                }

            }
        } catch (Exception e) {
            log.error("consumer heartbeat 异常" ,e);
        }
        return data;
    }

    public void shutdown() {

    }
}
