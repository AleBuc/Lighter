package com.alebuc.lighter.configuration;

import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmbedMongoConfigurationTest {

    private final EmbedMongoConfiguration embedMongoConfiguration = EmbedMongoConfiguration.INSTANCE;

    @AfterAll
    public static void tearDown() {
        EmbedMongoConfiguration embedMongoConfiguration = EmbedMongoConfiguration.INSTANCE;
        if (embedMongoConfiguration.getRunning() != null && embedMongoConfiguration.getRunning().current().isAlive()) {
            embedMongoConfiguration.getRunning().close();
        }
    }

    @Test
    @Order(1)
    void shouldStartDatabase() {
        assertThat(embedMongoConfiguration.getRunning()).isNull();
        embedMongoConfiguration.startMongoDB();
        assertThat(embedMongoConfiguration.getRunning()).isNotNull();
        assertThat(embedMongoConfiguration.getRunning().current().isAlive()).isTrue();
        assertThat(embedMongoConfiguration.getConnectionString()).isNotNull();
    }

    @Test
    @Order(2)
    void shouldStopDatabase() {
        assertThat(embedMongoConfiguration.getRunning()).isNotNull();
        embedMongoConfiguration.closeMongoDB();
        assertThat(embedMongoConfiguration.getRunning().current().isAlive()).isFalse();
    }

}
