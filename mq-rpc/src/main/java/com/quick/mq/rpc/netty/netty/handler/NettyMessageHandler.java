package com.quick.mq.rpc.netty.netty.handler;

import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.t_enum.CompressType;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.t_enum.SerializeType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class NettyMessageHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        try {
            if (message instanceof NettyMessage){
                log.info("Server received message: {}", message);
                // 处理Client Request
                NettyMessage responseMsg = this.handleRequestMsg((NettyMessage) message);
                // 响应请求
                ctx.writeAndFlush(responseMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 确保ByteBuf内存释放，防止内存溢出
            ReferenceCountUtil.release(message);
        }
    }

    private NettyMessage handleRequestMsg(NettyMessage message) {
        log.info("处理 消息");

        NettyMessage resp = new NettyMessage("ok", CompressType.SNAPPY.getC(), SerializeType.JSON.getB(),
            MessageType.RESPONSE.getB());
        resp.setMsgId(message.getMsgId());
        return resp;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client exception", cause);
        ctx.channel().close();
    }
}