package com.alebuc.lighter.command;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.alebuc.lighter.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.standard.commands.Quit;

@Slf4j
@Command(group = "Lighter")
@RequiredArgsConstructor
public class LighterCommand {

    private final KafkaService kafkaService;

    @Command(command = "consume", description = "Consume a given topic.")
    public void consume(String topicName) {
        kafkaService.addTopicConsumer(topicName);
    }

    @Command(group = "Built-In Commands")
    @RequiredArgsConstructor
    public static class CustomQuit implements Quit.Command {
        private final EmbedMongoConfiguration mongoConfiguration;
        private final KafkaService kafkaService;
        @Command(command = "quit", alias = "exit", description = "Exit the shell.")
        public void quit() throws InterruptedException {
            log.info("Closing consumers...");
            kafkaService.stopListener();
            log.info("Closing the database...");
            mongoConfiguration.closeMongoDB();
            throw new ExitRequest();
        }
    }
}
