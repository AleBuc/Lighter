package com.alebuc.lighter.configuration;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.plain.internals.PlainSaslServer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private static final String GROUP_ID = "Lighter";
    private final KafkaProperties kafkaProperties;

    public Properties getProperties() {
        Properties properties = new Properties();
        KafkaProperties.ConnectionProperties serverProperties = kafkaProperties.getServer();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverProperties.getAddress());
        if (StringUtils.isNotBlank(serverProperties.getUsername()) && StringUtils.isNotBlank(serverProperties.getPassword())) {
            properties.setProperty(
                    SaslConfigs.SASL_JAAS_CONFIG,
                    String.format("%s required username=\"%s\" password= \"%s\";", PlainLoginModule.class.getName(), serverProperties.getUsername(), serverProperties.getPassword()));
        }
        KafkaProperties.ConnectionProperties schemaRegistryProperties = kafkaProperties.getSchemaRegistry();
        if (schemaRegistryProperties != null && StringUtils.isNotBlank(schemaRegistryProperties.getAddress())) {
            properties.setProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryProperties.getAddress());
            if (StringUtils.isNotBlank(schemaRegistryProperties.getUsername()) && StringUtils.isNotBlank(schemaRegistryProperties.getPassword())) {
                properties.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM);
                properties.setProperty(SaslConfigs.SASL_MECHANISM, PlainSaslServer.PLAIN_MECHANISM);
                properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name());
                properties.setProperty(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
                properties.setProperty(SchemaRegistryClientConfig.USER_INFO_CONFIG, String.format("%s:%s", schemaRegistryProperties.getUsername(), schemaRegistryProperties.getPassword()));
            }
        }
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverProperties.getAddress());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return properties;
    }

    @Bean
    public DefaultKafkaConsumerFactory<Object, Object> getKafkaConsumerFactory() {
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return new DefaultKafkaConsumerFactory<>(map);
    }

}
