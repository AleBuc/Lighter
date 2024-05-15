package com.alebuc.lighter.command;

import com.alebuc.lighter.configuration.mongodb.EmbedMongoConfiguration;
import com.alebuc.lighter.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.commands.Quit;

/**
 * Lighter Spring Shell commands
 *
 * @author AleBuc
 */
@Slf4j
@Command(group = "Lighter")
@RequiredArgsConstructor
public class LighterCommand {

    private final KafkaService kafkaService;

    /**
     * Consumes a topic from its name.
     * @param topicName topic's name
     */
    @Command(command = "consume", description = "Consume a given topic (use 'help consume' for more details).")
    public void consume(
            String topicName,
            @Option(longNames = "key-type", shortNames = 'k', description = "Key type of the events topic", defaultValue = "string") String keyType,
            @Option(longNames = "value-type", shortNames = 'v', description = "Value type of the events topic", defaultValue = "string") String valueType) {
        kafkaService.addTopicConsumer(topicName, keyType, valueType);
    }

    /**
     * Stops a topic listener.
     * @param topicName topic name
     */
    @Command(command = "stop", description = "Stop a running listener.")
    public void stopListener(String topicName) {
        kafkaService.stopListener(topicName);
    }

    /**
     * Customization of quit command to stop consumers and embedded database when quit.
     */
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
