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
    private  String clientId = null;

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
        do {
            log.info("发送心跳");
            //心跳 20s一次
            this.heartbeat();
            waitForRunning(20_000L);
        }while (!this.isStopped());

        log.info(this.getServiceName() + " service end");
    }

    private void heartbeat() {
        log.info("consumer 发送心跳");

        NettyClient client = new NettyClient();
        ConsumerNode node = new ConsumerNode(consumerGroup, topic, this.clientId);
        Message message = new Message(null ,JSONObject.toJSONString(node), MessageType.HEARTBEAT);
        try {
            Response send = client.send(message);
            if (send.getStatus() == Response.OK){
                Object result = send.getResult();
                ConsumerNode tmp = JSONObject.parseObject((String) result, ConsumerNode.class);
                this.clientId = this.clientId == null ? tmp.getClientId() : this.clientId;
            }
        } catch (Exception e) {
            log.error("consumer heartbeat 异常" ,e);
        }
    }
}
