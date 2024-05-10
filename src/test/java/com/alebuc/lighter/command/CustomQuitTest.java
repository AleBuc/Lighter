package com.alebuc.lighter.command;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.configuration.mongodb.EmbedMongoConfiguration;
import com.alebuc.lighter.service.KafkaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.shell.ExitRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomQuitTest {

    @InjectMocks
    private LighterCommand.CustomQuit customQuit;
    @Mock
    private EmbedMongoConfiguration mongoConfiguration;
    @Mock
    private KafkaService kafkaService;

    @Test
    void shouldQuit() throws InterruptedException {
        //GIVEN
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(LighterCommand.class)).addAppender(logWatcher);
        //WHEN
        assertThatThrownBy(() -> customQuit.quit())
        //THEN
                .isInstanceOf(ExitRequest.class);
        verify(kafkaService).stopListener();
        verify(mongoConfiguration).closeMongoDB();
        assertThat(logWatcher.list)
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("Closing consumers...");
                })
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("Closing the database...");
                });
    }

}
