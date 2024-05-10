package com.alebuc.lighter.configuration.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private ConnectionProperties server;
    private ConnectionProperties schemaRegistry;

    @Data
    public static class ConnectionProperties {
        private String address;
        private String username;
        private String password;
    }
}
