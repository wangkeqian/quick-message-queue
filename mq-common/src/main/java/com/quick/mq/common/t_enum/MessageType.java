package com.quick.mq.common.t_enum;

public enum MessageType {

    REQUEST((byte) 0x00, "identity"),
    RESPONSE((byte) 0x01, "gzip"),
    HEARTBEAT((byte) 0x02, "bzip2"),

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
