package com.quick.mq.common.t_enum;

public enum CompressType {

    IDENTITY((byte) 0x00, "identity"),
    GZIP((byte) 0x01, "gzip"),
    BZIP2((byte) 0x02, "bzip2"),
    SNAPPY((byte) 0x03, "snappy");

    ;

    private final byte c;
    private final String desc;

    CompressType(byte c, String desc) {
        this.c = c;
        this.desc = desc;
    }

    public byte getC() {
        return c;
    }

    public String getDesc() {
        return desc;
    }
}
