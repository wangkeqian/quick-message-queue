package com.quick.mq.netty.server.handler;

import com.quick.mq.netty.codec.FastMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class FastMessageHandler extends SimpleChannelInboundHandler<FastMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FastMessage message) throws Exception {

    }
}
