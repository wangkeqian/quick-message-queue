package com.quick.mq;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.netty.server.FastMqServer;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

/**
 * Hello world!
 *
 */
@Slf4j
public class QuickMqServer
{
    public static final String fastMqServerName = "/fast_message_queue/server_list";

    public static void main( String[] args ) throws Exception {

        FastMqServer fastMqServer = new FastMqServer();
        initZkRegister(8050);
        fastMqServer.bind(8050);

        MyShutdownHook shutdownHook = new MyShutdownHook();
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }
    private static void initZkRegister(int port) throws Exception {

        ServiceNode serviceNode = ServiceNode.builder()
                .host("127.0.0.1")
                .port(8050)
                .build();
        CuratorFramework zkClient = new ZookeeperConfig().zookeeperClient;
        zkClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(fastMqServerName + "/" + port,JSONObject.toJSONString(serviceNode).getBytes());


        List<String> ports = zkClient.getChildren().forPath(fastMqServerName);
        for (String p : ports) {
            byte[] bytes = zkClient.getData().forPath(fastMqServerName + "/" + p);
            log.info(new String(bytes));
        }
    }
    static class MyShutdownHook implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            CuratorFramework zkClient = new ZookeeperConfig().zookeeperClient;
            zkClient.delete()
                    .forPath(fastMqServerName + "/" + 8050);
        }
    }
}
