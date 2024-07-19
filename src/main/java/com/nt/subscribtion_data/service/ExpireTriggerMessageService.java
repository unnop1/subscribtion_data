package com.nt.subscribtion_data.service;

import java.sql.SQLException;
import java.util.List;
import com.nt.subscribtion_data.model.dao.DataModel.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nt.subscribtion_data.entity.view.consumer_ordertype.ConsumerLJoinOrderType;
import com.nt.subscribtion_data.log.LogFlie;

@Service
public class ExpireTriggerMessageService {
    
    @Autowired
    private DistributeService distributeService;

    public void logExpiredTriggerMessage(Long orderTypeID, Data sendData) {

        try{
            List<ConsumerLJoinOrderType> conOdt = distributeService.ListConsumerOrderTypes(orderTypeID);

            for (ConsumerLJoinOrderType consumer : conOdt) {
                // System expired log
                LogFlie.logMessage(
                    "subscribtion_data",
                    String.format("trigger-expire/%s/%s",LogFlie.dateFolderName(), consumer.getCONSUMER_GROUP()),
                    sendData
                );
            }
        }catch(Exception e){
            
        }
    }

}
