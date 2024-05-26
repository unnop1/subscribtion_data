package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;

@Service
public class CATMFEService {

    @Value("${data-mapping.host}")
    private String host;

    @Value("${data-mapping.port}")
    private String port;

    @Value("${data-mapping.context}")
    private String context="/";

    private CATMFEClient client;

    public OfferingSpecData getOfferingSpecByOfferingId(String offeringId){
        client = new CATMFEClient(host, port, context);
        return client.GetOfferingSpecByOfferingId(offeringId);
    }
}
