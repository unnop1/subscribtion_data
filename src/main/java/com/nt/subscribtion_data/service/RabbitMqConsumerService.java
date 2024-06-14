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
import com.nt.subscribtion_data.model.dao.DataModel.Data;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private MappingService mappingService;

    @RabbitListener(queues = {"RedsRechargeQ", "RedsOrderQ", "RedsPackageExpireQ"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) throws JsonMappingException, JsonProcessingException {
        try{
            String queueName = (String) headers.get("amqp_receivedRoutingKey");
            // System.out.println("Received message from queue " + queueName + ": " + message);
            // Process the message based on the queue name
            Data sendData = null;
            switch (queueName) {
                case "RedsRechargeQ":
                    sendData = mappingService.processTopUpType(message);
                    break;
                case "RedsOrderQ":
                    sendData = mappingService.processOMType(message);
                    break;
                case "RedsPackageExpireQ":
                    sendData = mappingService.processExpiredType(message);
                    break;
                default:
                    // System.out.println("Unknown queue: " + queueName);
                    break;
            }

            if (sendData != null){
                kafkaProducerService.sendMessage(sendData.getOrderType(),"", sendData);
                // System.out.println("sending to kafka");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
