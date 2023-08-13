package com.quick.mq.rpc.netty.netty;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.quick.mq.common.exception.RemotingException;
import com.quick.mq.common.exchange.DefaultNettyFuture;
import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.rpc.netty.netty.channel.NettyChannel;
import com.quick.mq.rpc.netty.netty.channel.NettyChannel.ChannelWrapper;
import com.quick.mq.rpc.netty.netty.codec.NetworkDecoder;
import com.quick.mq.rpc.netty.netty.codec.NetworkEncoder;
import com.quick.mq.rpc.netty.netty.factory.EventLoopGroupFactory;
import com.quick.mq.rpc.netty.netty.handler.NettyClientRequestHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient extends AbstractNettyClient {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    protected void openConnect() {
        this.bootstrap = new Bootstrap();
        this.eventLoopGroup = createEventLoopGroup();
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new NetworkEncoder())
                        .addLast(new NetworkDecoder())
                        .addLast(new NettyClientRequestHandler());
                }
            });
    }

    private EventLoopGroup createEventLoopGroup() {
        return EventLoopGroupFactory.eventLoopGroup(1 , "netty_client_worker_thread");
    }

    protected Response doSend(ServiceNode serv, NettyMessage request)
        throws InterruptedException, ExecutionException, TimeoutException {
        InetSocketAddress address = new InetSocketAddress(serv.getHost(), serv.getPort());
        ChannelWrapper channelWrapper = NettyChannel.getOrCreateNewChannel(address, () -> {
                    return doConnect(address);
            });
        long msgId = request.getMsgId();
        DefaultNettyFuture nettyFuture = DefaultNettyFuture.newFuture(msgId);
        channelWrapper.send(request);

        return (Response) nettyFuture.get(10_000, TimeUnit.MILLISECONDS);
    }

    private ChannelFuture doConnect(InetSocketAddress address) {
        ChannelFuture channelFuture = this.bootstrap.connect(address);
        boolean connected = channelFuture.awaitUninterruptibly(30_000, MILLISECONDS);

        if (connected && channelFuture.isSuccess()) {
            log.info("The client has connected [{}] success.", address.toString());
            return channelFuture;
        } else if (channelFuture.cause() != null) {
            // Failed to connect to provider server by other reason
            Throwable cause = channelFuture.cause();
            log.error("Failed to connect to provider server by other reason.", cause);
            throw new RemotingException("Client failed to connect to server " + address + ", error message is:" + cause.getMessage(), cause);
        } else {
            // Client timeout
            RemotingException remotingException = new RemotingException("client failed to connect to server " + address + " client  timeout " + 60000);
            log.error("Client timeout.", remotingException);
            throw remotingException;
        }
    }

    public void close() {

    }
}
