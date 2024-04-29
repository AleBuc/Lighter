package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.KafkaConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    @Getter
    private boolean isListening = false;
    private final KafkaConfiguration kafkaConfiguration;
    private final Map<String, KafkaConsumer<Object, Object>> consumerMap = new HashMap<>();

    public void addTopicConsumer(String topic) {
        if (consumerMap.containsKey(topic)){
            return;
        }
        // use KafkaMessageListenerContainer instead (https://stackoverflow.com/a/51729490)
        KafkaConsumer<Object, Object> consumer = kafkaConfiguration.getConsumer();
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
        }
        consumerMap.put(topic, consumer);
    }


    public void consumeTopics() {
        isListening = true;
        List<KafkaConsumer<Object, Object>> consumers = consumerMap.values().stream().toList();
        while (isListening) {
            for (KafkaConsumer<Object, Object> consumer : consumers) {
                consumer.wakeup();
                ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Object, Object> event : records) {
                    log.info("New event! Key: {}, Value: {}", event.key(), event.value());
                }
            }
        }
        for (KafkaConsumer<Object, Object> consumer : consumers) {
            consumer.close();
        }
    }

    public void stopListener() {
        isListening = false;
    }
}
