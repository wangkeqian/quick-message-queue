package com.quick.mq.common.exchange;

import com.quick.mq.common.t_enum.CompressType;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.t_enum.SerializeType;
import com.quick.mq.common.t_enum.VersionEnum;

public class NettyMessage extends Request{

    public NettyMessage(Object data) {
        super(data);
        this.compressType = CompressType.SNAPPY.getC();
        this.serializeType = SerializeType.JSON.getB();
        this.messageType = MessageType.REQUEST.getB();
    }
    public NettyMessage(Object data, byte compressType, byte serializeType, byte messageType) {
        super(data);
        this.compressType = compressType;
        this.serializeType = serializeType;
        this.messageType = messageType;
    }

    /**
     * 版本
     */
    private final VersionEnum version = VersionEnum.V1;
    /**
     * 压缩类型
     */
    private byte compressType;
    /**
     * 序列化类型
     */
    private byte serializeType;
    /**
     * 消息类型
     */
    private byte messageType;

    public VersionEnum getVersion() {
        return version;
    }

    public byte getCompressType() {
        return compressType;
    }

    public void setCompressType(byte compressType) {
        this.compressType = compressType;
    }

    public byte getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(byte serializeType) {
        this.serializeType = serializeType;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }
}
