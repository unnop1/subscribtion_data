package com.nt.subscribtion_data.service;

import java.util.Map;

// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqConsumerService {

    

    @RabbitListener(queues = {"new_order_type", "suspended_order_type"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) {
        String queueName = (String) headers.get("amqp_receivedRoutingKey");
        System.out.println("Received message from queue " + queueName + ": " + message);

        // Process the message based on the queue name
        switch (queueName) {
            case "RedsRechargeQ":
                processTopUpType(message);
                break;
            case "RedsOrderQ":
                processOMType(message);
                break;
            default:
                System.out.println("Unknown queue: " + queueName);
                break;
        }
    }

    private void processTopUpType(String message) {
        // Process for new_order_type
        // kafkaTemplate.send("new_order", message);
    }

    private void processOMType(String message) {
        // Process for new_order_type 
        // kafkaTemplate.send("suspend_order", message);
    }
}
