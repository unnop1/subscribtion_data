package com.nt.subscribtion_data.service;

import java.sql.SQLException;
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
import com.nt.subscribtion_data.model.dao.DataModel.TriggerMessageData;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private DistributeService distributeService;

    @RabbitListener(queues = {"RedsRechargeQ", "RedsOrderQ", "RedsPackageExpireQ"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) throws JsonMappingException, JsonProcessingException {
        try{
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
                case "RedsPackageExpireQ":
                    processExpiredType(message);
                    break;
                default:
                    System.out.println("Unknown queue: " + queueName);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOMType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingOMData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void processTopUpType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingTopUpData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void processExpiredType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingExpiredData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void MappingOMData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
        
    }

    private void MappingTopUpData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
    }

    private void MappingExpiredData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
    }
}
