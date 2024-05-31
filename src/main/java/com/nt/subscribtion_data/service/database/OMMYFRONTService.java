package com.nt.subscribtion_data.service.database;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.OMMYFRONTClient;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;

@Service
public class OMMYFRONTService {

    @Value("${ommyfront.host}")
    private String host;

    @Value("${ommyfront.port}")
    private String port;

    @Value("${ommyfront.context}")
    private String context="/";

    private OMMYFRONTClient client;

    public OrderHeaderData getOrderHeaderDataByOrderID(String orderId){
        client = new OMMYFRONTClient(host, port, context);
        return client.GetOfferHeaderById(orderId);
    }

    public OrderHeaderData getOrderHeaderDataByICCID(String iccid){
        client = new OMMYFRONTClient(host, port, context);
        return client.GetOfferHeaderByIccid(iccid);
    }

    public OrderHeaderData getOrderHeaderDataByPoID(Long poId){
        client = new OMMYFRONTClient(host, port, context);
        return client.GetOfferHeaderByPoId(poId);
    }

    public List<IMSIOfferingConfig> getImsiOfferingConfigList(){
        client = new OMMYFRONTClient(host, port, context);
        return client.GetListIMSIOfferingConfig();
    }
}
