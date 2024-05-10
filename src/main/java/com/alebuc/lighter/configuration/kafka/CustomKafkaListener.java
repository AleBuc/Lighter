package com.alebuc.lighter.configuration.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration to always read a topic from offset 0
 */
@Component
public class CustomKafkaListener extends AbstractConsumerSeekAware {
    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        for (Map.Entry<TopicPartition, Long> entry : assignments.entrySet()) {
            TopicPartition topicPartition = entry.getKey();
            callback.seek(topicPartition.topic(), topicPartition.partition(), 0L);
        }
    }
}
