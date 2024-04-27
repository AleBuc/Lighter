package com.alebuc.lighter.command;

import com.alebuc.lighter.service.KafkaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LighterCommandTest {

    @InjectMocks
    private LighterCommand lighterCommand;
    @Mock
    private KafkaService kafkaService;

    private static final String TOPIC_NAME = "testTopic";

    @Test
    void shouldConsumeTopic() {
        //WHEN
        lighterCommand.consume(TOPIC_NAME);
        //THEN
        List<Thread> threads = (List<Thread>) ReflectionTestUtils.getField(lighterCommand, "kafkaConsumers");
        assertThat(threads).isNotEmpty()
                .hasSize(1);
    }
}