package com.quick.mq.client.task;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.quick.mq.client.consumer.DefaultConsumer;
import com.quick.mq.client.consumer.PullRequest;
import com.quick.mq.common.exchange.ConsumerNode;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.PullMessageRequest;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.utils.ServiceThread;
import com.quick.mq.rpc.netty.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ConsumerPushMessageService extends ServiceThread {

    private DefaultConsumer defaultConsumer;
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
            PullMessageRequest request = new PullMessageRequest(topic, queueId);
            Message message = new Message(topic , JSONObject.toJSONString(request), MessageType.CONSUMER_PRE_PUll_REQUEST);
            try {
                Response resp = client.send(message);
                if (resp.getStatus() == Response.OK){
                    if (ObjectUtil.isNotEmpty(resp.getResult()) && !resp.getResult().equals("null")){
                        Map result = JSONObject.parseObject((String) resp.getResult(),Map.class);
                        if (ObjectUtil.isNotEmpty(result)){
                            PullRequest pullRequest = new PullRequest();
                            Integer startOffset = (Integer) result.get("startOffset");
                            Integer endOffset = (Integer) result.get("endOffset");
                            pullRequest.setCqStartOffset(startOffset);
                            pullRequest.setCqEndOffset(endOffset);
                            defaultConsumer.addPullRequestQueue(pullRequest);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("consumer heartbeat 异常" ,e);
            }
        }while (!this.isStopped());

        log.info(this.getServiceName() + " service end");
    }


    public void start(String topic ,int queueId ,DefaultConsumer defaultConsumer) {
        this.topic = topic;
        this.queueId = queueId;
        this.defaultConsumer = defaultConsumer;
        super.start();
    }
}
