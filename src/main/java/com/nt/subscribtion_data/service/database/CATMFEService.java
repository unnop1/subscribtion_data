package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;

@Service
public class CATMFEService {

    @Value("${catmfe.host}")
    private String host;

    @Value("${catmfe.port}")
    private String port;

    @Value("${catmfe.context}")
    private String context="/";

    private CATMFEClient client;

    public OfferingSpecData getOfferingSpecByOfferingId(String offeringId){
        client = new CATMFEClient(host, port, context);
        return client.GetOfferingSpecByOfferingId(offeringId);
    }
}
