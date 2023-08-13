package com.quick.mq.common.exchange;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

public abstract class Request {

    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    private long msgId;

    private Object data;

    public Request(Object data) {
        this.data = data;
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
}
