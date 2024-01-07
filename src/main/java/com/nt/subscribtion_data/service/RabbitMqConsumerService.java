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

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; 

    @RabbitListener(queues = {"new_order_type", "suspended_order_type"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) {
        String queueName = (String) headers.get("amqp_receivedRoutingKey");
        System.out.println("Received message from queue " + queueName + ": " + message);

        // Process the message based on the queue name
        switch (queueName) {
            case "new_order_type":
                processNewOrderType(message);
                break;
            case "suspended_order_type":
                processSuspendedOrderType(message);
                break;
            default:
                System.out.println("Unknown queue: " + queueName);
                break;
        }
    }

    private void processNewOrderType(String message) {
        // Process for new_order_type
        System.out.println("Processing new_order_type: " + message);
        kafkaTemplate.send("new_order", message);
    }

    private void processSuspendedOrderType(String message) {
        // Process for new_order_type 
        System.out.println("Processing new_order_type: " + message);
        kafkaTemplate.send("suspend_order", message);
    }
}
