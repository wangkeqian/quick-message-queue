package com.quick.mq.nameserv.config.zk;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.config.NettyServerConfig;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import com.quick.mq.nameserv.config.NamesServConfig;
import com.quick.mq.nameserv.config.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Slf4j
public class ZookeeperDiscovery implements ServiceDiscovery {

    public static final String fastMqServerName = "/fast_message_queue/server_list";
    private final CuratorFramework zkClient;
    private final NamesServConfig namesServConfig;
    private final NettyServerConfig nettyServerConfig;
    private final String zkNodePath;
    public ZookeeperDiscovery(NamesServConfig namesServConfig ,NettyServerConfig nettyServerConfig) {
        this.namesServConfig = namesServConfig;
        this.nettyServerConfig = nettyServerConfig;
        zkClient = new ZookeeperConfig(namesServConfig.getNameServHostWithPort()).zookeeperClient;
        this.zkNodePath = fastMqServerName + "/" + nettyServerConfig.getServerPort();
    }

    public ServiceNode findServ() {
        List<String> ports;
        try {
            ports = zkClient.getChildren().forPath(fastMqServerName);
        }catch (Exception e){
            log.info("zk客户端异常" ,e);
            throw new RuntimeException("zk客户端异常");
        }
        if (ObjectUtil.isEmpty(ports)){
            throw new RuntimeException("无MQ服务节点启用");
        }
        String port = ports.get(0);
        byte[] info;
        try{
            info = zkClient.getData().forPath(zkNodePath);
        }catch (Exception e){
            log.info("zk客户端搜索服务异常" ,e);
            throw new RuntimeException("zk客户端搜索服务异常");
        }
        return JSONObject.parseObject(new String(info), ServiceNode.class);
    }

    public void register() {
        int serverPort = nettyServerConfig.getServerPort();
        try {
            Stat stat = zkClient.checkExists().forPath(zkNodePath);
            if (stat == null){
                zkClient.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(zkNodePath,JSONObject.toJSONString(nettyServerConfig).getBytes());
            }
        } catch (Exception e) {
            log.info("zk客户端注册异常" ,e);
            throw new RuntimeException("zk客户端异常");
        }
    }

    public void removeServ() {
        log.info("broker应用停止");
        try {
            zkClient.delete().guaranteed().forPath(zkNodePath);
        } catch (Exception e) {
            log.info("zk客户端删除异常" ,e);
            throw new RuntimeException("zk客户端异常");
        }

    }

}
