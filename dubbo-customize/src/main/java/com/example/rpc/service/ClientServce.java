package com.example.rpc.service;

import com.example.rpc.ioc.annotation.Component;
import com.example.rpc.ioc.annotation.Reference;

/**
 * @author Lz
 * @date 2025/3/7 11:03
 * @since 1.0.0
 */
@Component
public class ClientServce {

    @Reference
    private ServerService serverService;

    public String sayHello(String name) {
        return serverService.sayHello(name);
    }
}
