package com.quick.mq.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.netty.codec.NetworkEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NettyClient {

    private int port = 8050;
    private String host = "127.0.0.1";

    public void connect(Object body, final ResponseCallback responseCallback){

        NioEventLoopGroup group = new NioEventLoopGroup();
        try{

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new NetworkEncoder())
                                    .addLast(new NettyClientRequestHandler(responseCallback));
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().writeAndFlush(JSONObject.toJSONString(body));
            f.channel().closeFuture().sync();
        }catch (Exception ex){
            log.error("netty客户端异常");
            ex.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }


    public interface ResponseCallback {
        void onResponse(String response);
    }
    public void doTest2(){


    }
}
