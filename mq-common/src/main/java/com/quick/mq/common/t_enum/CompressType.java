package com.quick.mq.common.t_enum;

public enum CompressType {

    GZIP('g',"GZIP"),
    SNAPPY('s',"Snappy")

    ;

    private final char c;
    private final String desc;

    CompressType(char c, String desc) {
        this.c = c;
        this.desc = desc;
    }

    public char getC() {
        return c;
    }

    public String getDesc() {
        return desc;
    }
}
