package com.nt.subscribtion_data.component;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;
import com.nt.subscribtion_data.service.DistributeService;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.ListIMSIOfferingConfigClientResp;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
@Component
public class CacheUpdater {
    // The cache variable
    private ListIMSIOfferingConfigClientResp imsiOfferConfigList;

    private List<OrderTypeEntity> orderTypes;

    private List<SaChannelConEntity> saChannelConnects;

    @Autowired
    private OMMYFRONTService ommyfrontService;

    @Autowired
    private DistributeService distributeService;

    // IMSIOfferingConfig
    @Scheduled(fixedRate = 600000) // 600000 milliseconds = 10 minutes
    public void refreshIMSIOfferConfigListCache() {
        imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();
    }

    // Getter for the cache variable
    public ListIMSIOfferingConfigClientResp getIMSIOfferConfigListCache() {
        // System.out.println("len cache = " + imsiOfferConfigList.size());
        return imsiOfferConfigList;
    }

    public void setIMSIOfferConfigListCache(ListIMSIOfferingConfigClientResp cacheList) {
        imsiOfferConfigList = cacheList;
    }

    // OrderType
    @Scheduled(fixedRate = 600000) // 600000 milliseconds = 10 minutes
    public void refreshOrderTypeListCache() throws SQLException {
        orderTypes = distributeService.LisOrderTypes();
    }

    public List<OrderTypeEntity> getOrderTypeListCache() {
        // System.out.println("len cache = " + orderTypes.size());
        return orderTypes;
    }

    public void setOrderTypeListCache(List<OrderTypeEntity> cacheList) {
        orderTypes = cacheList;
    }

    // saChannelConnects
    @Scheduled(fixedRate = 600000) // 600000 milliseconds = 10 minutes
    public void refreshSaChannelConnectListCache() throws SQLException {
        saChannelConnects = distributeService.ListChannelConnect();
    }

    public List<SaChannelConEntity> getSaChannelConnectListCache() {
        // System.out.println("len cache = " + orderTypes.size());
        return saChannelConnects;
    }

    public void setSaChannelConnectListCache(List<SaChannelConEntity> cacheList) {
        saChannelConnects = cacheList;
    }
    
}

