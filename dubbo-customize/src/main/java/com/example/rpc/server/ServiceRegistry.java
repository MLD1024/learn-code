package com.example.rpc.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public static void registerService(String serviceName, Object service) {
        serviceMap.put(serviceName, service);
    }

    public static Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }
}