package com.quick.mq.config;

import lombok.Data;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/8/19
 */
@Data
public class NettyServerConfig {
  private String serverHost;
  private int serverPort = 9898;
  //服务工作线程
  private int servWordThreads = 8;
  private int serverSelectorThreads = 3;


}
