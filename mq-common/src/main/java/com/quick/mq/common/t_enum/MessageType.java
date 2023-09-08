package com.quick.mq.common.t_enum;

public enum MessageType {

    PRODUCER_REQUEST((byte) 0x00, "客户端请求"),
    RESPONSE((byte) 0x01, "响应"),
    HEARTBEAT((byte) 0x02, "心跳"),
    CONSUMER_REGISTER_REQUEST((byte) 0x03, "消费端注册请求"),
    CONSUMER_PRE_PUll_REQUEST((byte) 0x04, "消费端预拉取消息请求"),
    CONSUMER_PUll_REQUEST((byte) 0x05, "消费端实际取消息请求"),
    ;

    private final byte b;
    private final String desc;

    MessageType(byte b, String desc) {
        this.b = b;
        this.desc = desc;
    }

    public byte getB() {
        return b;
    }

    public String getDesc() {
        return desc;
    }
}
