package com.example.rpc.registry;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ZookeeperRegistry implements RegistryService {
    private static final String BASE_PATH = "/rpc";
    private final ZooKeeper zk;
    private final ConcurrentHashMap<String, List<String>> serviceCache = new ConcurrentHashMap<>();

    public ZookeeperRegistry(String connectString) throws Exception {
        zk = new ZooKeeper(connectString, 3000, watchedEvent -> {
            // 处理节点变化事件
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                String path = watchedEvent.getPath();
                if (path != null) {
                    serviceCache.remove(path.substring(BASE_PATH.length() + 1));
                }
            }
        });
        initRootNode();
    }

    private void initRootNode() throws KeeperException, InterruptedException {
        if (zk.exists(BASE_PATH, false) == null) {
            zk.create(BASE_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    @Override
    public void register(String serviceName, String serviceAddress) throws Exception {
        String servicePath = BASE_PATH + "/" + serviceName;
        if (zk.exists(servicePath, false) == null) {
            zk.create(servicePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        String addressPath = servicePath + "/address-";
        zk.create(addressPath, serviceAddress.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public List<String> discover(String serviceName) throws Exception {
        String servicePath = BASE_PATH + "/" + serviceName;
        List<String> addresses = zk.getChildren(servicePath, true);
        return addresses
                .stream()
                .map(addr -> {
                    try {
                        return new String(zk.getData(servicePath + "/" + addr, false, new Stat()));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    throw new RuntimeException("Failed to get data from zookeeper");
                })
                .collect(Collectors.toList());
    }
}