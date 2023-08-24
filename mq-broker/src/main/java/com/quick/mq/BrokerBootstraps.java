package com.quick.mq;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.utils.MixAll;
import com.quick.mq.common.utils.ServerUtil;
import com.quick.mq.common.zookeeper.ZookeeperConfig;
import com.quick.mq.common.config.BrokerConfig;
import com.quick.mq.nameserv.config.NamesServConfig;
import com.quick.mq.store.config.MessageStoreConfig;
import com.quick.mq.config.NettyClientConfig;
import com.quick.mq.config.NettyServerConfig;
import com.quick.mq.controller.BrokerController;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.cli.CommandLine;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;
import org.apache.zookeeper.data.Stat;

/**
 * start all in
 */
@Slf4j
public class BrokerBootstraps
{
    public static final String fastMqServerName = "/fast_message_queue/server_list";
    public static Properties properties = null;
    public static CommandLine commandLine = null;
    public static String configFile = null;
    public static void main( String[] args ) throws Exception {

        LetsGo(createBrokerController(args));

    }

    private static void LetsGo(BrokerController brokerController) {
        brokerController.start();
    }

    private static BrokerController createBrokerController(String[] args) {

        Options options = ServerUtil.buildCommandlineOptions(new Options());
        commandLine = ServerUtil.parseCmdLine("mqbroker", args, buildCommandlineOptions(options),
            new PosixParser());
        if (null == commandLine) {
            System.exit(-1);
        }

        final BrokerConfig brokerConfig = new BrokerConfig();
        final MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
        final NettyServerConfig nettyServerConfig = new NettyServerConfig();
        final NettyClientConfig nettyClientConfig = new NettyClientConfig();
        final NamesServConfig namesServConfig = new NamesServConfig();

        try {
            if (commandLine.hasOption('c')) {
                String brokerConfFile = commandLine.getOptionValue('c');
                if (ObjectUtil.isNotEmpty(brokerConfFile)) {
                    //配置读取
                    configFile = brokerConfFile;
                    properties = new Properties();
                    InputStream in = new BufferedInputStream(new FileInputStream(brokerConfFile));
                    properties.load(in);
                    MixAll.properties2Object(properties, brokerConfig);
                    MixAll.properties2Object(properties, nettyServerConfig);
                    MixAll.properties2Object(properties, nettyClientConfig);
                    MixAll.properties2Object(properties, messageStoreConfig);
                    in.close();
                }
            }else {
                log.error("启动失败, 需要配置 -c broken.conf 路径");
                System.exit(-1);
            }

            final BrokerController brokerController = new BrokerController(
                brokerConfig,
                messageStoreConfig,
                nettyServerConfig,
                nettyClientConfig,
                namesServConfig);

            boolean init = brokerController.init();
            if (!init){
                log.error("brokerController Init fail");
                brokerController.shutdown();
                System.exit(-2);
            }

            return brokerController;

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static void initZkRegister(int port) throws Exception {

        ServiceNode serviceNode = ServiceNode.builder()
                .host("127.0.0.1")
                .port(8050)
                .build();
        CuratorFramework zkClient = new ZookeeperConfig(namesServConfig.getNameServHostWithPort()).zookeeperClient;
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
            CuratorFramework zkClient = new ZookeeperConfig(namesServConfig.getNameServHostWithPort()).zookeeperClient;
            zkClient.delete()
                    .forPath(fastMqServerName + "/" + 8050);
        }
    }
    private static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("c", "configFile", true, "Broker config properties file");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "printConfigItem", false, "Print all config item");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("m", "printImportantConfig", false, "Print important config item");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }
}
