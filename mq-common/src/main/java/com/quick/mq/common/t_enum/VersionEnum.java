package com.quick.mq.common.t_enum;

public enum VersionEnum {
    V1((byte) 0x00, "1.0.1")

    ;

    private byte version;
    private String desc;

    VersionEnum(byte version, String desc) {
        this.version = version;
        this.desc = desc;
    }

    public byte getVersion() {
        return version;
    }
}
