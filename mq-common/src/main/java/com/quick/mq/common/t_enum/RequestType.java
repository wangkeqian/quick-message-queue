package com.quick.mq.common.t_enum;

public enum RequestType {

    REQUEST('r',"请求"),
    RESPONSE('e',"响应")

    ;

    private final char c;
    private final String desc;

    RequestType(char c, String desc) {
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
