package com.alebuc.lighter.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.configuration.KafkaConfiguration;
import com.alebuc.lighter.configuration.KafkaProperties;
import com.alebuc.lighter.utils.JsonFormatter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    void init() {
        this.logWatcher = new ListAppender<>();
        this.logWatcher.start();
        ((Logger) LoggerFactory.getLogger(KafkaService.class)).addAppender(this.logWatcher);
    }

    @Test
    void shouldAddTopicConsumer() throws InterruptedException {
        //GIVEN
        try (MockedStatic<BeanUtils> mockedStatic = Mockito.mockStatic(BeanUtils.class)) {
            String testTopic = "testTopic";
            KafkaConsumer<Object, Object> kafkaConsumer = Mockito.mock(KafkaConsumer.class);
            KafkaProperties.ConnectionProperties serverProperties = new KafkaProperties.ConnectionProperties();
            serverProperties.setAddress("http://localhost:29092");
            KafkaProperties kafkaProperties = new KafkaProperties();
            kafkaProperties.setServer(serverProperties);
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(kafkaProperties);
            KafkaService kafkaService = new KafkaService(kafkaConfiguration, kafkaConfiguration.getKafkaConsumerFactory());

            //WHEN
            kafkaService.addTopicConsumer(testTopic);
            //THEN
            assertThat(kafkaService.getContainers())
                    .hasSize(1);

        }
    }

    @Test
    void shouldStopConsumers() {
        //GIVEN
        KafkaMessageListenerContainer<Object,Object> kafkaMessageListenerContainer = Mockito.mock(KafkaMessageListenerContainer.class);
        List<KafkaMessageListenerContainer<Object,Object>> kafkaMessageListenerContainers = new ArrayList<>();
        kafkaMessageListenerContainers.add(kafkaMessageListenerContainer);
        KafkaService kafkaService = new KafkaService(mock(KafkaConfiguration.class), mock(DefaultKafkaConsumerFactory.class));
        ReflectionTestUtils.setField(kafkaService, "containers", kafkaMessageListenerContainers);
        //WHEN
        kafkaService.stopListener();
        //THEN
        verify(kafkaMessageListenerContainer).stop();


    }

}
