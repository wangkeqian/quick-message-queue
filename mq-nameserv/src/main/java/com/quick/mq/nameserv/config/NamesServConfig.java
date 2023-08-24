package com.quick.mq.nameserv.config;


import lombok.Data;

/**
 * 服务发现配置
 */
@Data
public class NamesServConfig {
    private String namServName;
    private String nameServHostWithPort;

}
