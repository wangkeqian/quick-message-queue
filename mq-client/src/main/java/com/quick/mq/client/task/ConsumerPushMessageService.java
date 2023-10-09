package com.quick.mq.client.task;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ConsumerNode;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.PullMessageRequest;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.utils.ServiceThread;
import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerPushMessageService extends ServiceThread {
    private String topic;
    private int queueId;

    @Override
    public String getServiceName() {
        return ConsumerPushMessageService.class.getSimpleName();
    }

    public void run() {
        NettyClient client = new NettyClient();
        log.info(this.getServiceName() + " service started");
        do {
            log.info("拉取开始");
            PullMessageRequest request = new PullMessageRequest(topic, queueId);
            Message message = new Message(topic , JSONObject.toJSONString(request), MessageType.CONSUMER_PRE_PUll_REQUEST);
            try {
                Response resp = client.send(message);
                if (resp.getStatus() == Response.OK){
                    String result = (String) resp.getResult();
                    log.info("本次从第 {} 开始 拉取",result);
                }
            } catch (Exception e) {
                log.error("consumer heartbeat 异常" ,e);
            }
        }while (!this.isStopped());

        log.info(this.getServiceName() + " service end");
    }

    private void pushMessage(int queueId, String topic, NettyClient client) {


    }

    public void start(String topic ,int queueId) {
        this.topic = topic;
        this.queueId = queueId;
        super.start();
    }
}
