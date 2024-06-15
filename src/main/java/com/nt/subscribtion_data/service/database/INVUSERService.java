package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.INVUSERClient;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingClientResp;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;

@Service
public class INVUSERService {

    @Value("${invuser.host}")
    private String host;

    @Value("${invuser.context}")
    private String context="/";

    private INVUSERClient client;

    public INVMappingClientResp getInvMappingData(String id){
        client = new INVUSERClient(host, context);
        return client.GetINVMappingByExternalId(id);
    }

    // public List<INVMasterData> getListINVMasterData(){
    //     client = new INVUSERClient(host, port, context);
    //     return client.GetINVMaster();
    // }
}
