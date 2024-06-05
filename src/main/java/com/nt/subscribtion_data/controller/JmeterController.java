package com.nt.subscribtion_data.controller;

import java.sql.Timestamp;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nt.subscribtion_data.service.MappingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.component.CacheUpdater;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;
import com.nt.subscribtion_data.service.DistributeService;
import com.nt.subscribtion_data.util.DateTime;

import jakarta.websocket.server.PathParam;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JmeterController {

    @Autowired
    private CacheUpdater cacheUpdater;

    @Autowired
    private DistributeService distributeService;

    @Autowired
    private MappingService mappingService;

    @PostMapping("/subscribe/{type}")
    public ResponseEntity<Boolean> subscribeData(@PathVariable("type") String subscribeType, @RequestBody String bodyMessage) {
        try {
            // System.out.println("subscribeType: " + subscribeType);

            ObjectMapper objectMapper = new ObjectMapper();

            List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
            if (orderTypes == null){
                orderTypes = distributeService.LisOrderTypes();
            }

            List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
            if (saChannels == null){
                saChannels = distributeService.ListChannelConnect();
            } 
            
            OrderTypeEntity orderTypeInfo;
            SaChannelConEntity saChannelConInfo;

            Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();

            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();

            switch (subscribeType.toLowerCase()) {
                case "om":
                    ReceiveOMDataType receivedOMData = objectMapper.readValue(bodyMessage, ReceiveOMDataType.class);
                    String orderType = receivedOMData.getOrderType().toUpperCase();
                    
                    orderTypeInfo = mappingService.getOrderTypeInfoFromList(orderType, orderTypes);
                    if (orderTypeInfo != null){
                        saChannelConInfo = mappingService.getSaChannelInfoFromList("OM", saChannels);
                        if (saChannelConInfo != null){
                            triggerMsg.setMESSAGE_IN(bodyMessage);
                            triggerMsg.setIS_STATUS(1);
                            triggerMsg.setORDERID(receivedOMData.getOrderId());
                            triggerMsg.setOrderType_Name(orderType);
                            triggerMsg.setOrderType_id(orderTypeInfo.getID());
                            triggerMsg.setPUBLISH_CHANNEL("OM-MFE");
                            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                            triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                            triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                            distributeService.CreateTriggerMessage(triggerMsg);
                        }
                    }
                    break;
                case "top":
                    // TOPUP_RECHARGE
                    String orderTypeName = "TOPUP_RECHARGE";
                    // String channelType = "TOPUP";
                    String publishChannelType = "Topup-GW";
                    
                    orderTypeInfo = mappingService.getOrderTypeInfoFromList(orderTypeName, orderTypes);
                    saChannelConInfo = mappingService.getSaChannelInfoFromList("OM", saChannels);
                    if (orderTypeInfo != null){
                        if (saChannelConInfo != null){
                            triggerMsg.setMESSAGE_IN(bodyMessage);
                            triggerMsg.setIS_STATUS(1);
                            triggerMsg.setOrderType_Name(orderTypeName);
                            triggerMsg.setOrderType_id(orderTypeInfo.getID());
                            triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                            triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                            triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                            distributeService.CreateTriggerMessage(triggerMsg);
                        }
                    }
                    break;
                default:
                    return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mapping")
    public ResponseEntity<Boolean> mappingData(@RequestBody String bodyMessage) {
        try {
            // System.out.println("bodyMessage:"+ bodyMessage);
            mappingService.processOMType(bodyMessage);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
