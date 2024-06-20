package com.nt.subscribtion_data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nt.subscribtion_data.service.MappingService;

import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.service.KafkaProducerService;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JmeterController {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private MappingService mappingService;

    @PostMapping("/subscribe/{type}")
    public ResponseEntity<Boolean> subscribeData(@PathVariable("type") String subscribeType, @RequestBody String bodyMessage) {
        try {
            switch (subscribeType.toLowerCase()) {
                case "om":
                    mappingService.processDefaultType(bodyMessage, false);
                    break;
                case "topup":
                    mappingService.processTopUpType(bodyMessage, false);
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

    @PostMapping("/mapping/{type}")
    public ResponseEntity<Object> mappingData(@PathVariable("type") String mappingType, @RequestBody String bodyMessage) {
        Data sendData=null;
        try {
            // System.out.println("bodyMessage:"+ bodyMessage);
            switch (mappingType.toLowerCase()) {
                case "om":
                    sendData = mappingService.processDefaultType(bodyMessage, true);
                    
                    break;
                case "topup":
                    sendData = mappingService.processTopUpType(bodyMessage, true);
                    break;
                default:
                    return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }


            if (sendData != null){
                kafkaProducerService.sendMessage(sendData.getOrderType(),"", sendData);
                // System.out.println("sending to kafka");
            }

            return new ResponseEntity<>(sendData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(sendData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
