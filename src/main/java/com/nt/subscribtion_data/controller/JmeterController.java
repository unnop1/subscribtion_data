package com.nt.subscribtion_data.controller;

import java.sql.Clob;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.nt.subscribtion_data.service.MappingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.component.CacheUpdater;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.service.DistributeService;
import com.nt.subscribtion_data.util.DateTime;


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

    @PostMapping("/mapping")
    public ResponseEntity<Boolean> mappingData(@PathVariable("type") String mappingType, @RequestBody String bodyMessage) {
        try {
            // System.out.println("bodyMessage:"+ bodyMessage);
            switch (mappingType.toLowerCase()) {
                case "om":
                    mappingService.processDefaultType(bodyMessage, true);
                    break;
                case "topup":
                    mappingService.processTopUpType(bodyMessage, true);
                    break;
                default:
                    return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
