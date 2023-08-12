package com.quick.mq.nameserv.zk;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Collections2;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import com.quick.mq.nameserv.ServiceDiscovery;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZookeeperDiscovery implements ServiceDiscovery {

    public static final String fastMqServerName = "/fast_message_queue/server_list";
    private final CuratorFramework zkClient;

    public ZookeeperDiscovery() {
        zkClient = new ZookeeperConfig().zookeeperClient;
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

}
