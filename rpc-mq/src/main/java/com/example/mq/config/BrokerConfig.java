package com.example.mq.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lz
 * @date 2025/3/7 17:00
 * @since 1.0.0
 */
@Getter
@Setter
public class BrokerConfig {
    public static String brokerName = "broker-1";
    public static String brokerIp = "127.0.0.1";
    public static int listenPort = 8888;
    public static String storePath = "/tmp/commitlog";
    public static int maxMessageSize = 1024 * 1024 * 4;
    public static int flushInterval = 1000;
    public static boolean clusterEnable = false;
}
