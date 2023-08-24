package com.quick.mq.nameserv.zk;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import com.quick.mq.nameserv.ServiceDiscovery;
import com.quick.mq.nameserv.config.NamesServConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

@Slf4j
public class ZookeeperDiscovery implements ServiceDiscovery {

    public static final String fastMqServerName = "/fast_message_queue/server_list";
    private final CuratorFramework zkClient;
    private final NamesServConfig namesServConfig;

    public ZookeeperDiscovery(NamesServConfig namesServConfig) {
        this.namesServConfig = namesServConfig;
        zkClient = new ZookeeperConfig(namesServConfig.getNameServHostWithPort()).zookeeperClient;
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
            info = zkClient.getData().forPath(fastMqServerName + "/" + port);
        }catch (Exception e){
            log.info("zk客户端异常" ,e);
            throw new RuntimeException("zk客户端异常");
        }
        return JSONObject.parseObject(new String(info), ServiceNode.class);
    }

    public void register() {

    }

    public void removeServ() {

    }

}
