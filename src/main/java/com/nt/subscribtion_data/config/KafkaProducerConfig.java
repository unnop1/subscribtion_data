package com.nt.subscribtion_data.config;

import java.util.HashMap; 
import java.util.Map; 
import org.apache.kafka.clients.producer.ProducerConfig; 
import org.apache.kafka.common.serialization.StringSerializer; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory; 
import org.springframework.kafka.core.DefaultKafkaProducerFactory; 
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class KafkaProducerConfig {

   
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put("security.protocol", "SASL_PLAINTEXT");
        configProps.put("sasl.mechanism", "SCRAM-SHA-256");
        configProps.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"admin\" password=\"admin-secret\";");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

