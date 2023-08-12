package com.quick.mq.common.t_enum;

public enum SerializeType {

    JSON('j',"JSON"),
    XML('x',"XML"),

    ;

    private final char c;
    private final String desc;

    SerializeType(char c, String desc) {
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
