package com.nt.subscribtion_data.service.database;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.INVUSERClient;
import com.nt.subscribtion_data.client.OMMYFRONTClient;
import com.nt.subscribtion_data.client.OMUSERClient;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;

@Service
public class INVUSERService {

    @Value("${invuser.host}")
    private String host;

    @Value("${invuser.port}")
    private String port;

    @Value("${invuser.context}")
    private String context="/";

    private INVUSERClient client;

    public INVMappingData getInvMappingData(String id){
        client = new INVUSERClient(host, port, context);
        return client.GetINVMappingByExternalId(id);
    }

    // public List<INVMasterData> getListINVMasterData(){
    //     client = new INVUSERClient(host, port, context);
    //     return client.GetINVMaster();
    // }
}
