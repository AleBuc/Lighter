package com.alebuc.lighter.service;

import com.alebuc.lighter.utils.JsonFormatter;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

@MicronautTest(packages = "com.alebuc.lighter.service")
@Testcontainers
class KafkaServiceTest {
    @Inject
    private KafkaService kafkaService;

    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    String topicName = "testTopic";
    String bootstrapServers;
    KafkaProducer<String, String> producer;

    @Captor
    private ArgumentCaptor<ConsumerRecords<Object, Object>> consumerRecordsArgumentCaptor;

    @BeforeEach
    void init() {
        kafka.start();
        bootstrapServers = kafka.getBootstrapServers();
        producer = new KafkaProducer<>(
                ImmutableMap.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.CLIENT_ID_CONFIG, "producer-test"
                ),
                new StringSerializer(),
                new StringSerializer()
        );
    }

    @AfterEach
    void close() {
        kafka.stop();
    }

    @Test
    void shouldConsumeTopic() throws InterruptedException {
        //GIVEN
        String testTopic = "testTopic";

        String key1 = "key1";
        Map<String, Object> testValueObject1 = new LinkedHashMap<>();
        testValueObject1.put("id", "1234");
        testValueObject1.put("count", 10);
        String key2 = "key2";
        String testValueObject2 = "value2";
        producer.send(new ProducerRecord<>(topicName, key1, JsonFormatter.parseObjectToJson(testValueObject1)));
        producer.send(new ProducerRecord<>(topicName, key2, testValueObject2));

        try (MockedStatic<EventService> mockedStatic = Mockito.mockStatic(EventService.class)) {
            //WHEN
            Thread consumptionThread = new Thread(() -> kafkaService.consumeTopic(bootstrapServers, testTopic));
            Thread stopThread = new Thread(kafkaService::stopListener);
            consumptionThread.start();
            Thread.sleep(1000);
            stopThread.start();

            //THEN
            EventService eventService = mock(EventService.class);
            mockedStatic.verify(() -> eventService.saveEvents(consumerRecordsArgumentCaptor.capture()));
            ConsumerRecords<Object, Object> consumerRecords = consumerRecordsArgumentCaptor.getValue();
            Assertions.assertThat(consumerRecords)
                    .isNotEmpty()
                    .hasSize(2)
                    .anyMatch(record -> record.partition() == 0
                                        && record.offset() == 0
                                        && record.key().equals(key1)
                                        && record.value().equals(testValueObject1))
                    .anyMatch(record -> record.partition() == 0
                                        && record.offset() == 1
                                        && record.key().equals(key2)
                                        && record.value().equals(testValueObject2));
        }

    }

}
