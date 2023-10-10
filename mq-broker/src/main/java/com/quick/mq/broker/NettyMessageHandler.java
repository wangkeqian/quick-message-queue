package com.quick.mq.broker;

import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.Response;
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
                Response responseMsg = this.handleRequestMsg((Message) message);
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
    private Response handleRequestMsg(Message message) {
        log.info("消息 {}" ,message.toString());
        Response response = new Response();
        switch (message.getMessageType()){
            case 0x00: //客户端请求
                boolean result = brokerController.acceptMessage(message);
                break;
            case 0x01:
                break;
            case 0x02: // 消费者心跳&注册
                response = brokerController.heartbeat(message);
                break;
            case 0x03:
                break;
            case 0x04: //消费端预拉请求
                response = brokerController.prePull(message);
                break;
            case 0x05:
                response = brokerController.Pull(message);
                break;
            default:
                throw new IllegalArgumentException("未知消息类型");
        }

        response.setMsgId(message.getMsgId());
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client exception", cause);
        ctx.channel().close();
    }
}
