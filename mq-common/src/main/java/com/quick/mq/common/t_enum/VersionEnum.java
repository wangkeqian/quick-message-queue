package com.quick.mq.common.t_enum;

public enum VersionEnum {

    V1("1.0.1","V1版本");


    private String version;
    private String desc;

    VersionEnum(String version, String desc) {
        this.version = version;
        this.desc = desc;
    }
}
