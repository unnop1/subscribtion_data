package com.nt.subscribtion_data.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.nt.subscribtion_data.repo.TriggerRepo;
import com.nt.subscribtion_data.repo.OrderTypeRepo;
import com.nt.subscribtion_data.repo.SaChannelConnectRepo;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
@Component
public class DistributeService {

    @Autowired
    private TriggerRepo triggerRepo;

    @Autowired
    private OrderTypeRepo orderTypeRepo;

    @Autowired
    private SaChannelConnectRepo saChannelConRepo;

    public TriggerMessageEntity CreateTriggerMessage(TriggerMessageEntity triggerMessageData) throws SQLException {
        triggerRepo.save(triggerMessageData);
        return triggerMessageData;
    }

    public List<OrderTypeEntity> LisOrderTypes() throws SQLException {
        List<OrderTypeEntity> orderTypeList = orderTypeRepo.ListOrderType();
        return orderTypeList;
    }

    public List<SaChannelConEntity> ListChannelConnect() throws SQLException {
        List<SaChannelConEntity> saChannelList = saChannelConRepo.ListChannelConnect();
        return saChannelList;
    }

    
}
