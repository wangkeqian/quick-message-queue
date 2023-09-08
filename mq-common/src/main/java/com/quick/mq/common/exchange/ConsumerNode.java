package com.quick.mq.common.exchange;

/**
 * 消费者节点
 */
public class ConsumerNode {
    private final String group;
    private final String topic;
    private final String clientId;


    public ConsumerNode(String group, String topic, String clientId) {
        this.group = group;
        this.topic = topic;
        this.clientId = clientId;
    }

    public String getGroup() {
        return group;
    }

    public String getTopic() {
        return topic;
    }

    public String getClientId() {
        return clientId;
    }
}
