package com.quick.mq.client.task;


import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ConsumerNode;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.utils.ServiceThread;
import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerHeartBeatService extends ServiceThread {

    private final String topic;
    private final String consumerGroup;

    public ConsumerHeartBeatService(String consumerGroup ,
                                    String topic) {
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }

    public String getServiceName() {
        return ConsumerHeartBeatService.class.getSimpleName();
    }

    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            log.info("等待20秒 发送心跳");
            //心跳 20s一次
            waitForRunning(20_000L);
            this.heartbeat();
        }

        log.info(this.getServiceName() + " service end");
    }

    private void heartbeat() {
        log.info("consumer 发送心跳");

        NettyClient client = new NettyClient();
        ConsumerNode node = new ConsumerNode(consumerGroup, topic, "1001");
        Message message = new Message(null ,node, MessageType.HEARTBEAT);
        try {
            Response send = client.send(message);
        } catch (Exception e) {
            log.error("consumer heartbeat 异常" ,e);
            throw new RuntimeException(e);
        }
    }
}
