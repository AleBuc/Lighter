package com.alebuc.lighter.configuration;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;

import java.util.Collection;
import java.util.Map;

public class Consumer implements ConsumerSeekAware {
    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        ConsumerSeekAware.super.registerSeekCallback(callback);
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        for (Map.Entry<TopicPartition, Long> entry : assignments.entrySet()) {
            entry.setValue(0L);
        }
        ConsumerSeekAware.super.onPartitionsAssigned(assignments, callback);
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        ConsumerSeekAware.super.onPartitionsRevoked(partitions);
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        ConsumerSeekAware.super.onIdleContainer(assignments, callback);
    }

    @Override
    public void onFirstPoll() {
        ConsumerSeekAware.super.onFirstPoll();
    }

    @Override
    public void unregisterSeekCallback() {
        ConsumerSeekAware.super.unregisterSeekCallback();
    }
}
