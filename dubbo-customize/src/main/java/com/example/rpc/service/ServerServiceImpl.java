package com.example.rpc.service;

import com.example.rpc.ioc.annotation.Service;

/**
 * @author Lz
 * @date 2025/3/7 11:02
 * @since 1.0.0
 */
@Service
public class ServerServiceImpl implements ServerService{
    @Override
    public String sayHello(String name) {
        return "Hello " + name + "!";
    }
}
