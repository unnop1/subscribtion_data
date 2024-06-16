package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.OMUSERClient;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLClientResp;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;

@Service
public class OMUSERService {

    @Value("${omuser.host}")
    private String host;

    @Value("${omuser.context}")
    private String context="/";

    private OMUSERClient client;

    public TransManageContractDTLClientResp getTransManageContractDTLData(String id){
        client = new OMUSERClient(host, context);
        return client.GetTransManageContractByTMasterId(id);
    }
}
