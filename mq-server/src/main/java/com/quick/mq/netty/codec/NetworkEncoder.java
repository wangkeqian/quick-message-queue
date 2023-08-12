package com.quick.mq.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NetworkEncoder extends MessageToByteEncoder {
    public static final byte[] MAGIC_NUM = {(byte) 'l', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final int HEAD_LENGTH = 16;
    private FastMessage body;

    public void setBody(FastMessage body) {
        this.body = body;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {

        // 1. header
        byte[] header = copyOf(MAGIC_NUM, HEAD_LENGTH);
        // 设置 version, compress, codec, message type
        header[4] = 1;
        header[5] = 'w';
        header[6] = 'k';
        header[7] = 'q';
        // 设置 message id
        Bytes.int2bytes(2, header, 8);

        // 2. encode request data
        byte[] dataBytes = String.valueOf(o).getBytes(StandardCharsets.UTF_8);

        // 数据长度
        int dataLen = dataBytes != null ? dataBytes.length : 0;
        Bytes.int2bytes(dataLen, header, 12);
        // 3. 将header与request data写入ByteBuf
        out.writeBytes(header);
        if (dataBytes != null) {
            out.writeBytes(dataBytes);
        }
    }
    public static byte[] copyOf(byte[] src, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, length));
        return dest;
    }

}
