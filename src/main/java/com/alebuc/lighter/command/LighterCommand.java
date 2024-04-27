package com.alebuc.lighter.command;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.alebuc.lighter.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.standard.commands.Quit;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Command(group = "Lighter")
@RequiredArgsConstructor
public class LighterCommand {

    static List<Thread> kafkaConsumers = new ArrayList<>();

    private final KafkaService kafkaService;

    @Command(command = "consume", description = "Consume a given topic.")
    public void consume(String topicName) {
        Thread consumerThread = new Thread(() -> kafkaService.consumeTopic(topicName));
        kafkaConsumers.add(consumerThread);
        consumerThread.start();

    }

    @Command
    @RequiredArgsConstructor
    public static class CustomQuit implements Quit.Command {
        private final EmbedMongoConfiguration mongoConfiguration;
        @Command
        public void quit() {
            log.info("Closing consumers...");
            kafkaConsumers.forEach(Thread::interrupt);
            log.info("Closing the database...");
            mongoConfiguration.closeMongoDB();
        }
    }
}
