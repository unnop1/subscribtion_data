package com.nt.subscribtion_data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class TopUpService {
    @Autowired
    private JdbcDatabaseService jbdcDB;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; 
}
