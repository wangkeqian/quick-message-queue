package com.quick.mq.common.exchange;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

public abstract class Request {

    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    private long msgId;

    private final String topic;
    private final Object data;

    public Request(String topic ,Object data) {
        this.data = data;
        this.topic = topic;
        this.msgId = REQUEST_ID.getAndIncrement();
    }

    public long getMsgId() {
        return msgId;
    }

    public Object getData() {
        return data;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getTopic() {
        return topic;
    }
}
