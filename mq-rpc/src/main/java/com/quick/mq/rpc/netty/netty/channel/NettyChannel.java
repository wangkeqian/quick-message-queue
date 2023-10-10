package com.quick.mq.rpc.netty.netty.channel;

import com.quick.mq.common.exception.RemotingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * netty Channel
 *
 * @author wangkq
 * @date 2023/8/12
 */
@Slf4j
public class NettyChannel {
  private static final long LOCK_TIMEOUT_MILLIS = 3_000;
  private static final ReentrantLock lockChannelTables = new ReentrantLock();
  private static final Condition channelInitialized = lockChannelTables.newCondition();

  private static final ConcurrentHashMap<Integer ,ChannelWrapper> CHANNEL_TABLE = new ConcurrentHashMap<>();
  public static ChannelWrapper getOrCreateNewChannel(InetSocketAddress address, Supplier<ChannelFuture> channelSupplier) throws InterruptedException {

    if (address == null){
      return null;
    }
    int addressHash = Objects.hash(address.getHostString() + address.getPort());
    ChannelWrapper cw = CHANNEL_TABLE.get(addressHash);
    if (cw != null && cw.isOK()){
      return cw;
    }

    if (lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)){
      try {
        cw = getChannelWrapper(addressHash ,channelSupplier);
      }catch (Exception e) {
        log.error("try get channel fail ..");
        throw new RuntimeException(e);
      }finally {
        lockChannelTables.unlock();
      }
    }else {
      log.info("等待锁");
      lockChannelTables.lock();
      try {
        while (cw == null || !cw.isOK()) {
          channelInitialized.await();
          cw = CHANNEL_TABLE.get(addressHash);
        }
      } finally {
        lockChannelTables.unlock();
      }
    }
    return cw;
  }


  private static ChannelWrapper getChannelWrapper(int addressHash, Supplier<ChannelFuture> channelSupplier) {

    ChannelWrapper channelWrapper = CHANNEL_TABLE.get(addressHash);

    boolean createNewChannel;
    if (channelWrapper != null){

      if (channelWrapper.isOK()){
        return channelWrapper;
      }else if (!channelWrapper.getChannelFuture().isDone()){
        return channelWrapper;
      }else {
        createNewChannel = true;
        CHANNEL_TABLE.remove(addressHash);
      }
    }else {
      createNewChannel = true;
    }
    if (createNewChannel){

      ChannelFuture future = channelSupplier.get();
      ChannelWrapper cw = new ChannelWrapper(future);
      log.info("cw 活跃状态 {}" ,cw.isOK());
      CHANNEL_TABLE.put(addressHash , cw);
      return cw;
    }
    return null;
  }


  public static class ChannelWrapper {
    private final ChannelFuture channelFuture;

    public ChannelWrapper(ChannelFuture channelFuture) {
      this.channelFuture = channelFuture;
    }

    public void send(Object data){
      this.send(data ,30_000L);
    }

    public void send(Object data ,long timeout){
      boolean success;
      Channel channel = null;
      try {
        channel = this.channelFuture.channel();
        boolean active = channel.isActive();
        if (!active){
          throw new RemotingException("链接不活跃");
        }
        boolean open = channel.isOpen();
//        log.info("channel 状态 active : {} ,open : {}" ,active ,open);
        ChannelFuture channelFuture = channel.writeAndFlush(data);
        success = channelFuture.await(timeout, TimeUnit.MILLISECONDS);
        Throwable cause = channelFuture.cause();
        if (cause != null) {
          log.error("cause P " ,cause);
          throw cause;
        }
      } catch (Throwable e) {
        log.error("channel send failed ..", e);
        throw new RemotingException("Failed to send message to " + ", cause: " + e.getMessage(), e);
      }
      if (!success) {
        throw new RemotingException("Failed to send message to " + channel.remoteAddress().toString() + " in timeout(" + timeout + "ms) limit");
      }
    }

    public boolean isOK() {
      return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
    }

    public boolean isWritable() {
      return this.channelFuture.channel().isWritable();
    }

    private io.netty.channel.Channel getChannel() {
      return this.channelFuture.channel();
    }

    public ChannelFuture getChannelFuture() {
      return channelFuture;
    }
  }
}
