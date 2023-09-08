package com.quick.mq.rpc.netty.netty.codec;

import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.t_enum.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NetworkEncoder extends MessageToByteEncoder {
    public static final byte[] MAGIC_NUM = {(byte) 'l', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final int HEAD_LENGTH = 16;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object data, ByteBuf out) throws Exception {

        if (!(data instanceof Message)){
            return;
        }

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
        if (message.getMessageType() != MessageType.HEARTBEAT.getB()){
            dataBytes = String.valueOf(message.getData()).getBytes(StandardCharsets.UTF_8);
            // todo  compress and serialize
        }
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
