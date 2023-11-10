package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.KafkaConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public enum KafkaService {
    INSTANCE;

    private boolean isListening = false;

    public void consumeTopic(String bootstrapServer, String topic) {
        isListening = true;
        KafkaConfiguration kafkaConfiguration = KafkaConfiguration.INSTANCE;
        KafkaConsumer<String, Object> consumer = kafkaConfiguration.getConsumer(bootstrapServer);
        try (consumer) {
            consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    //NOOP
                }
                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    consumer.seekToBeginning(partitions);
                }
            });
            while (isListening) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Object> event : records) {
                    log.info("New event! Key: {}, Value: {}", event.key(), event.value());
                }
            }
        }
    }

    public void stopListener() {
        isListening = false;
    }
}
