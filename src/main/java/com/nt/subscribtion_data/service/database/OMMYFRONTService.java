package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.OMMYFRONTClient;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;

@Service
public class OMMYFRONTService {

    @Value("${data-mapping.host}")
    private String host;

    @Value("${data-mapping.port}")
    private String port;

    @Value("${data-mapping.context}")
    private String context="/";

    private OMMYFRONTClient client;

    public OrderHeaderData getOrderHeaderDataByOrderID(String orderId){
        client = new OMMYFRONTClient(host, port, context);
        return client.GetOfferingSpecById(orderId);
    }
}
