package com.quick.mq.rpc.netty.netty.handler;

import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.DefaultNettyFuture;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.t_enum.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientRequestHandler extends SimpleChannelInboundHandler<Object>  {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {
        try{
            log.info("client receive msg {}" ,message);
            if (message instanceof Message){
                Message msg = (Message) message;
                if (msg.getMessageType() == MessageType.HEARTBEAT.getB()){
                    log.info("this is heartbeat {}" ,msg.getData());
                    return;
                }
                if (msg.getMessageType() == MessageType.RESPONSE.getB()){
                    Response response = new Response();
                    response.setResult(msg.getData());
                    DefaultNettyFuture.sent(msg.getMsgId() ,response);
                }
            }else if (message instanceof Response){
                Response response = (Response) message;
                DefaultNettyFuture.sent(response.getMsgId() ,response);
            }
        }finally {
            ReferenceCountUtil.release(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }




}
