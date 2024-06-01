package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.OMUSERClient;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;

@Service
public class OMUSERService {

    @Value("${omuser.host}")
    private String host;

    @Value("${omuser.port}")
    private String port;

    @Value("${omuser.context}")
    private String context="/";

    private OMUSERClient client;

    public TransManageContractDTLData getTransManageContractDTLData(String id){
        client = new OMUSERClient(host, port, context);
        return client.GetTransManageContractByTMasterId(id);
    }
}
