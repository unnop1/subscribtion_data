package com.nt.subscribtion_data.service;

import java.util.Map;

// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private OMService omServices;

    @Autowired
    private TopUpService topUpService;

    @RabbitListener(queues = {"new_order_type", "suspended_order_type"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) throws JsonMappingException, JsonProcessingException {
        try{
            String queueName = (String) headers.get("amqp_receivedRoutingKey");
            System.out.println("Received message from queue " + queueName + ": " + message);
            // Process the message based on the queue name
            switch (queueName) {
                case "suspended_order_type":
                    processTopUpType(message);
                    break;
                case "new_order_type":
                    processOMType(message);
                    break;
                default:
                    System.out.println("Unknown queue: " + queueName);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTopUpType(String message) throws JsonMappingException, JsonProcessingException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveTopUpDataType receivedData = objectMapper.readValue(message, ReceiveTopUpDataType.class);
        topUpService.getTopUpQuery(receivedData);
    }

    private void processOMType(String message) throws JsonMappingException, JsonProcessingException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        omServices.getOMQuery(receivedData);
    }
}
