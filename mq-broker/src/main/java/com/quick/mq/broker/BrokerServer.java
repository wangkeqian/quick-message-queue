package com.quick.mq.broker;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.quick.mq.rpc.netty.netty.codec.NetworkDecoder;
import com.quick.mq.rpc.netty.netty.codec.NetworkEncoder;
import com.quick.mq.rpc.netty.netty.handler.NettyMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 服务端
 */
public class BrokerServer {

    public void bind(int port) throws Exception {
        //创建线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b
                    .group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024) // 设置tcp缓冲区
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 这是接收缓冲大小
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new NetworkDecoder())
                                    .addLast(new NetworkEncoder())
                                    .addLast("server-idle-handler", new IdleStateHandler(0, 0, 3, MINUTES))
                                    .addLast(new NettyMessageHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}