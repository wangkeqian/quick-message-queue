package com.quick.mq.rpc.netty.netty.codec;

import cn.hutool.extra.template.engine.freemarker.FreemarkerEngine;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.t_enum.VersionEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NetworkEncoder extends MessageToByteEncoder {
    public static final byte[] MAGIC_NUM = {(byte) 'l', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final int HEAD_LENGTH = 16;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object data, ByteBuf out) throws Exception {

        if (data instanceof Message){


            Message message = (Message) data;

            // 1. header
            byte[] header = copyOf(MAGIC_NUM, HEAD_LENGTH);
            // 设置 version, compress, codec, message type
            header[4] = message.getVersion().getVersion();
            header[5] = message.getCompressType();
            header[6] = message.getSerializeType();
            header[7] = message.getMessageType();
            // 设置 message id
            Bytes.int2bytes(message.getMsgId(), header, 8);

            // 2. encode request data
            byte[] dataBytes = null;
            dataBytes = String.valueOf(message.getData()).getBytes(StandardCharsets.UTF_8);
            // 数据长度
            int dataLen = dataBytes != null ? dataBytes.length : 0;
            Bytes.int2bytes(dataLen, header, 12);
            // 3. 将header与request data写入ByteBuf
            out.writeBytes(header);
            if (dataBytes != null) {
                out.writeBytes(dataBytes);
            }
        }else if (data instanceof Response){

            Response resp = (Response) data;

            // 1. header
            byte[] header = copyOf(MAGIC_NUM, HEAD_LENGTH);
            // 设置 version, compress, codec, message type
            header[4] = VersionEnum.V1.getVersion();
            header[5] = 0;
            header[6] = resp.getStatus();
            header[7] = MessageType.RESPONSE.getB();
            // 设置 message id
            Bytes.int2bytes(resp.getMsgId(), header, 8);

            // 2. encode request data
            byte[] dataBytes = null;
            dataBytes = String.valueOf(resp.getResult()).getBytes(StandardCharsets.UTF_8);
            // 数据长度
            int dataLen = dataBytes != null ? dataBytes.length : 0;
            Bytes.int2bytes(dataLen, header, 12);
            // 3. 将header与request data写入ByteBuf
            out.writeBytes(header);
            if (dataBytes != null) {
                out.writeBytes(dataBytes);
            }
        }
    }
    public static byte[] copyOf(byte[] src, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, length));
        return dest;
    }

}
