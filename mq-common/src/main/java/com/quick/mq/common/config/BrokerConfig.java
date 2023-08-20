package com.quick.mq.common.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * broker配置类
 *
 * @author wangkq
 * @date 2023/8/19
 */
@Slf4j
@Data
public class BrokerConfig {

  private final long brokerId = 1_001L;
  private final String brokerName = localHostName();
  private String brokerHost;
  private int brokerPort;
  private String nameServHostWithPort;
  private String nameServType;

  public static String localHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.error("Failed to obtain the host name", e);
    }

    return "DEFAULT_BROKER";
  }
}
