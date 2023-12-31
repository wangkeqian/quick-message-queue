package com.quick.mq.common.t_enum;

public enum SerializeType {
    JSON((byte) 0x00, "JSON"),
    XML((byte) 0x01, "XML"),
    ;

    private final byte b;
    private final String desc;

    SerializeType(byte b, String desc) {
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
