package com.quick.mq.broker;

import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.t_enum.CompressType;
import com.quick.mq.common.t_enum.MessageType;
import com.quick.mq.common.t_enum.SerializeType;
import com.quick.mq.controller.BrokerController;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class NettyMessageHandler extends ChannelInboundHandlerAdapter {


    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final BrokerController brokerController;

    public NettyMessageHandler(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        log.info("来了老弟");
        channels.add(ctx.channel());
        log.info("channelid = {} ，channels size = {}",ctx.channel().id(), channels.size());
        try {
            if (message instanceof Message){
                log.info("Server received producer message: {}", message);
                // 处理Client Request
                Message responseMsg = this.handleRequestMsg((Message) message);
                // 响应请求
                ctx.writeAndFlush(responseMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 确保ByteBuf内存释放，防止内存溢出
            ReferenceCountUtil.release(message);
        }
    }

    /**
     *
     PRODUCER_REQUEST((byte) 0x00, "客户端请求"),
     RESPONSE((byte) 0x01, "响应"),
     HEARTBEAT((byte) 0x02, "心跳"),
     CONSUMER_REQUEST((byte) 0x03, "消费端请求"),
     * @param message
     * @return
     */
    private Message handleRequestMsg(Message message) {
        log.info("消息 {}" ,message.toString());
        switch (message.getMessageType()){
            case 0x00: //客户端请求
                boolean result = brokerController.acceptMessage(message);
                break;
            case 0x01:
                break;
            case 0x02:
                brokerController.heartbeat(message);
                break;
            case 0x03: //消费端请求
//                brokerController.registerConsumer(message);
                break;
            default:
                throw new IllegalArgumentException("未知消息类型");
        }

        Message resp = new Message(message
                .getTopic(),"ok", CompressType.SNAPPY.getC(), SerializeType.JSON.getB(),
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
