package com.example.rpc.registry;

import java.util.List;

/**
 * @author Lz
 * @date 2025/3/4 14:36
 * @since 1.0.0
 */
public interface RegistryService {
    public void register(String serviceName, String serviceAddress) throws Exception ;

    public List<String> discover(String serviceName) throws Exception;
}
