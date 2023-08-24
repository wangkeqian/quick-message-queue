package com.quick.mq.common.zookeeper;

import com.quick.mq.common.exception.RemotingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

@Slf4j
public class ZookeeperConfig {
    private int timeout = 4000;

    public final CuratorFramework zookeeperClient;

    public ZookeeperConfig(String nameServHostWithPort){
        zookeeperClient = CuratorFrameworkFactory.builder()
                .connectString(nameServHostWithPort)
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(new ExponentialBackoffRetry(3000,10))
                .build();
        zookeeperClient.start();
        // 阻塞直到连接成功
        try {
            zookeeperClient.blockUntilConnected();
        }catch (Exception ex){
            throw new RemotingException("zk客户端链接异常");
        }
        log.info("初始化 zookeeperClient");

    }

}
