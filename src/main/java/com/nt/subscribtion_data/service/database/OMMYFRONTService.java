package com.nt.subscribtion_data.service.database;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.OMMYFRONTClient;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.ListIMSIOfferingConfigClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;

@Service
public class OMMYFRONTService {

    @Value("${ommyfront.host}")
    private String host;

    @Value("${ommyfront.context}")
    private String context="/";

    private OMMYFRONTClient client;

    public OrderHeaderClientResp getOrderHeaderDataByOrderID(String orderId){
        client = new OMMYFRONTClient(host, context);
        return client.GetOfferHeaderById(orderId);
    }

    public OrderHeaderClientResp getOrderHeaderDataByICCID(String iccid){
        client = new OMMYFRONTClient(host, context);
        return client.GetOfferHeaderByIccid(iccid);
    }

    public OrderHeaderClientResp getOrderHeaderDataByPoID(Long poId){
        client = new OMMYFRONTClient(host, context);
        return client.GetOfferHeaderByPoId(poId);
    }

    public ListIMSIOfferingConfigClientResp getImsiOfferingConfigList(){
        client = new OMMYFRONTClient(host, context);
        return client.GetListIMSIOfferingConfig();
    }
}
