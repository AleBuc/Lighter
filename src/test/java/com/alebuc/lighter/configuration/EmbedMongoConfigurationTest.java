package com.alebuc.lighter.configuration;

import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class EmbedMongoConfigurationTest {

    private final EmbedMongoConfiguration embedMongoConfiguration = EmbedMongoConfiguration.getInstance();
    private static InputStream originalSystemIn;
    private ByteArrayInputStream simulatedSystemIn;

    @BeforeAll
    public static void setup() {
        originalSystemIn = System.in;
    }
    @AfterAll
    public static void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    void startAndStopDatabase() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {

            try {
                embedMongoConfiguration.startMongoDB();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

        wait(completableFuture);

        TransitionWalker.ReachedState<RunningMongodProcess> running = embedMongoConfiguration.getRunning();
        assertThat(running.current().isAlive()).isTrue();


        simulatedSystemIn = new ByteArrayInputStream("\n\r".getBytes());
        System.setIn(simulatedSystemIn);
        Scanner scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());

        wait(completableFuture);

        assertThat(running.current().isAlive()).isFalse();


    }

    private void wait(CompletableFuture<Void> completableFuture) throws ExecutionException, InterruptedException {
        try {
            completableFuture.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {

        }
    }
}
