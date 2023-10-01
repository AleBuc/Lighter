package com.alebuc.lighter;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(name = "run", description = "Run the embedded database.")
public class Runner implements Callable<String> {
    @Override
    public String call() throws Exception {
        CommandLine.usage(this, System.out);
        EmbedMongoConfiguration configuration = EmbedMongoConfiguration.getInstance();
        configuration.startMongoDB();
        log.info("Connection string: {}", configuration.getConnectionString());
        log.info("Press ENTER when you want to quit.");
        try {
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            log.info("Closing database.");
            configuration.closeMongoDB();
        }
        log.info("Stopping the app.");
        return null;
    }
}
