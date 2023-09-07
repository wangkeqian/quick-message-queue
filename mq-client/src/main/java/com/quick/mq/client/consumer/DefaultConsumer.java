package com.quick.mq.client.consumer;

import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class DefaultConsumer implements Consumer{
    private final String consumerGroup;
    private String topic;
    private String nameServ;
    private final RebalanceImpl rebalanceImpl;
    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();

    private final NettyClient client;
    public DefaultConsumer() {
        this("default_group");
    }

    public DefaultConsumer(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        rebalanceImpl = new RebalancePushImpl(this);
        client = new NettyClient();
        init();
    }

    public void subscribe(String topic) {
        this.topic = topic;
    }

    public void setNameServ(String nameServ) {
        this.nameServ = nameServ;
    }

    private void init() {

    }

    public void start() throws InterruptedException {
        boolean result = false;
        //通知broker，本consumer启动了
        

        //重平衡机制


        while (true){
            PullRequest take = pullRequestQueue.take();
            pullMessage(take);
        }
    }

    private void pullMessage(PullRequest take) {
        log.info("终于拉到了 {}" ,take);
    }

    public void shutdown() {

    }
}
