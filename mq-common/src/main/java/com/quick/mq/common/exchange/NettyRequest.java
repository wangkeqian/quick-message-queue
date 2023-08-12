package com.quick.mq.common.exchange;

import com.quick.mq.common.t_enum.CompressType;
import com.quick.mq.common.t_enum.RequestType;
import com.quick.mq.common.t_enum.SerializeType;
import com.quick.mq.common.t_enum.VersionEnum;
import lombok.Data;

@Data
public class NettyRequest extends Request{
    /**
     * 版本
     */
    private final VersionEnum version = VersionEnum.V1;
    /**
     * 压缩类型
     */
    private final char compressType = RequestType.REQUEST.getC();
    /**
     * 序列化类型
     */
    private final char serializeType = SerializeType.JSON.getC();
    /**
     * 消息类型
     */
    private final char messageType = CompressType.SNAPPY.getC();



}
