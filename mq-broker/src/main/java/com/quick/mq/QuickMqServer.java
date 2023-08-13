package com.quick.mq;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.broker.BrokerServer;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;
import org.apache.zookeeper.data.Stat;

/**
 * Hello world!
 *
 */
@Slf4j
public class QuickMqServer
{
    public static final String fastMqServerName = "/fast_message_queue/server_list";

    public static void main( String[] args ) throws Exception {

        MyShutdownHook shutdownHook = new MyShutdownHook();
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));

        BrokerServer brokerServer = new BrokerServer();
        initZkRegister(8050);
        brokerServer.bind(8050);
    }
    private static void initZkRegister(int port) throws Exception {

        ServiceNode serviceNode = ServiceNode.builder()
                .host("127.0.0.1")
                .port(8050)
                .build();
        CuratorFramework zkClient = new ZookeeperConfig().zookeeperClient;
        String nodePath = fastMqServerName + "/" + port;

        Stat stat = zkClient.checkExists().forPath(nodePath);
        if (stat == null){
            zkClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(nodePath,JSONObject.toJSONString(serviceNode).getBytes());
        }
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
            log.info("清除zk 路由数据");
            CuratorFramework zkClient = new ZookeeperConfig().zookeeperClient;
            zkClient.delete()
                    .forPath(fastMqServerName + "/" + 8050);
        }
    }
}
