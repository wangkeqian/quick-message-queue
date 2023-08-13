package com.quick.mq.rpc.netty.netty.factory;

import com.quick.mq.common.utils.RemotingUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ThreadFactory;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/12
 */
public class EventLoopGroupFactory {


  public static EventLoopGroup eventLoopGroup(int i, String threadName) {
    ThreadFactory threadFactory = new DefaultThreadFactory(threadName, true);
    return useEpoll() ? new EpollEventLoopGroup(i ,threadFactory) :
        new NioEventLoopGroup(i ,threadFactory);
  }
  private static boolean useEpoll() {
    return RemotingUtil.isLinuxPlatform()
        && Epoll.isAvailable();
  }
}
