package com.example.mq.namesrv;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NameServer {
    private final ConcurrentHashMap<String/* topic */, List<BrokerData>> topicRouteMap = new ConcurrentHashMap<>();

    public void start() {
        new Thread(
                () -> {
                    while(true) {
                        try {
                            Thread.sleep(30000);
                            for (List<BrokerData> brokers : topicRouteMap.values()) {
                                brokers.removeIf(broker -> 
                                    System.currentTimeMillis() - broker.lastUpdateTimestamp > 30000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
    }

    public void registerBroker(String topic, String brokerName, String brokerAddr) {
        BrokerData brokerData = new BrokerData();
        brokerData.brokerName = brokerName;
        brokerData.brokerAddr = brokerAddr;
        brokerData.lastUpdateTimestamp = System.currentTimeMillis();
        topicRouteMap.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(brokerData);
    }

    private final AtomicInteger brokerIndex = new AtomicInteger(0);

    public List<BrokerData> pickupBrokers(String topic) {
        List<BrokerData> aliveBrokers = topicRouteMap.getOrDefault(topic, Collections.emptyList())
                .stream()
                .filter(broker -> System.currentTimeMillis() - broker.lastUpdateTimestamp <= 30000)
                .collect(Collectors.toList());

        if (!aliveBrokers.isEmpty()) {
            return aliveBrokers;
        }
        return Collections.emptyList();
    }

    public List<BrokerData> getAvailableBrokers(String topic) {
        return pickupBrokers(topic);
    }

    public List<String> getBrokerAddresses(String topic) {
        List<BrokerData> brokers = getAvailableBrokers(topic);
        if (!brokers.isEmpty()) {
            int index = brokerIndex.getAndIncrement() % brokers.size();
            return Collections.singletonList(brokers.get(index).brokerAddr);
        }
        return Collections.emptyList();
    }

    public static class BrokerData {
        public String brokerName;
        public String brokerAddr;
        public long lastUpdateTimestamp;
    }
}