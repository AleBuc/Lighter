package com.alebuc.lighter;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.alebuc.lighter.service.KafkaService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(name = "run", description = "Run the embedded database.")
public class Runner implements Callable<String> {

    @CommandLine.Option(names = {"-sa", "--server-address"}, description = "Bootstrap server address", required = true)
    String serverAddress = "";

    @CommandLine.Option(names = {"-t", "--topic"}, description = "Topic to consume", required = true)
    String topic = "";

    private final EmbedMongoConfiguration mongoConfiguration = EmbedMongoConfiguration.getInstance();
    private final KafkaService kafkaService = KafkaService.getInstance();

    @Override
    public String call() throws Exception {
        CommandLine.usage(this, System.out);
        mongoConfiguration.startMongoDB();
        Thread consumerThread = new Thread(() -> kafkaService.consumeTopic(serverAddress, topic));
        log.info("Connection string: {}", mongoConfiguration.getConnectionString());
        log.info("Press ENTER when you want to quit.");
        try {
            consumerThread.start();
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            log.info("Closing database.");
            mongoConfiguration.closeMongoDB();
            kafkaService.stopListener();
        }
        log.info("Stopping the app.");
        return null;
    }
}
