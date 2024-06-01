package com.nt.subscribtion_data.component;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;

@Component
public class CacheUpdater {
    // The cache variable
    private List<IMSIOfferingConfig> imsiOfferConfigList;

    @Autowired
    private OMMYFRONTService ommyfrontService;

    // The method to refresh the cache
    @Scheduled(fixedRate = 600000) // 600000 milliseconds = 10 minutes
    public void refreshIMSIOfferConfigListCache() {
        // Update the cache variable (for example, with the current timestamp)
        imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();
    }

    // Getter for the cache variable
    public List<IMSIOfferingConfig> getIMSIOfferConfigListCache() {
        System.out.println("len cache = " + imsiOfferConfigList.size());
        return imsiOfferConfigList;
    }

    public void setIMSIOfferConfigListCache(List<IMSIOfferingConfig> cacheList) {
        imsiOfferConfigList = cacheList;
    }
}

