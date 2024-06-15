package com.nt.subscribtion_data.service.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecClientResp;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;

@Service
public class CATMFEService {

    @Value("${catmfe.host}")
    private String host;

    @Value("${catmfe.context}")
    private String context="/";

    private CATMFEClient client;

    public OfferingSpecClientResp getOfferingSpecByOfferingId(String offeringId){
        client = new CATMFEClient(host, context);
        OfferingSpecClientResp  resp = client.GetOfferingSpecByOfferingId(offeringId);
        return resp;
    }
}
