package com.alebuc.lighter.configuration.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration to always read a topic from offset 0
 */
@Component
public class CustomConsumerSeekAware implements ConsumerSeekAware {
    private final Map<Thread, ConsumerSeekCallback> callbackForThread = new ConcurrentHashMap<>();
    private final Map<TopicPartition, ConsumerSeekCallback> callbacks = new ConcurrentHashMap<>();

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        this.callbackForThread.put(Thread.currentThread(), callback);
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        callback.seekToBeginning(assignments.keySet());
        assignments.keySet().forEach(partition -> this.callbacks.put(partition, callback));
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        partitions.forEach(this.callbacks::remove);
        this.callbackForThread.clear();
    }
}
