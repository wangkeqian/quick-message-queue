package com.quick.mq.client.consumer;

import com.quick.mq.client.task.ConsumerHeartBeatService;
import com.quick.mq.client.task.ConsumerPushMessageService;
import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

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
            pullMessage(take);
        }
    }

    public void addPullRequestQueue(PullRequest pullRequest){
        this.pullRequestQueue.add(pullRequest);
    }

    private void pullMessage(PullRequest take) {
        log.info("终于拉到了 起始偏移量 ：{}，结束偏移量 ：{}" ,take.getCqStartOffset() ,take.getCqEndOffset());
    }

    public void shutdown() {

    }
}
