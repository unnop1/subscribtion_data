package com.nt.subscribtion_data.service;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.nt.subscribtion_data.repo.TriggerRepo;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;

@Component
public class DistributeService {

    @Autowired
    private TriggerRepo triggerRepo;

    public TriggerMessageEntity CreateTriggerMessage(TriggerMessageEntity triggerMessageData) throws SQLException {
        triggerRepo.save(triggerMessageData);
        return triggerMessageData;
    }

    
}
