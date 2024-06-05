package com.nt.subscribtion_data.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Destination;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.json.JSONArray;
import org.json.JSONObject;

// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;
import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SaleInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.BalanceTransferInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ContractInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.CreditLimit;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Expire;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ExtendExpireInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Offer;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Photo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.SouthernContactAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.TopUp;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.VarietyService;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.Address;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.DestinationCustomerAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.VatAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.VatDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.DestinationSimInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.DestinationSubscriberInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.SourceSimInfo;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;
import com.nt.subscribtion_data.model.dto.ReceiveExpiredDataType;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;
import com.nt.subscribtion_data.service.database.CATMFEService;
import com.nt.subscribtion_data.service.database.INVUSERService;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;
import com.nt.subscribtion_data.service.database.OMUSERService;
import com.nt.subscribtion_data.util.DateTime;


import com.nt.subscribtion_data.component.CacheUpdater;

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
