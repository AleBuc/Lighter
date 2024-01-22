package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.KafkaConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class KafkaService {
    private static KafkaService INSTANCE;
    public static KafkaService getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new KafkaService(EventService.getInstance());
        }
        return INSTANCE;
    }
    private boolean isListening = false;
    private final EventService eventService;

    public void consumeTopic(String bootstrapServer, String topic) {
        isListening = true;
        KafkaConfiguration kafkaConfiguration = KafkaConfiguration.INSTANCE;
        KafkaConsumer<Object, Object> consumer = kafkaConfiguration.getConsumer(bootstrapServer);
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
                ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(100));
                eventService.saveEvents(records);
            }
        }
    }

    public void stopListener() {
        isListening = false;
    }
}
