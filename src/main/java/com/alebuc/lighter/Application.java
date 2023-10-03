package com.alebuc.lighter;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        logger.info("Run database");
        EmbedMongoConfiguration configuration = EmbedMongoConfiguration.getInstance();
        configuration.startMongoDB();
    }
}
