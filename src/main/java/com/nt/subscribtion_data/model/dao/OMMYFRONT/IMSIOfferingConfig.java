package com.nt.subscribtion_data.model.dao.OMMYFRONT;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IMSIOfferingConfig {
    @JsonProperty("id")
    private int id;

    @JsonProperty("imsi_PREFIX")
    private String imsiPrefix;

    @JsonProperty("allow4G")
    private String allow4G;

    @JsonProperty("allow5G")
    private String allow5G;
}
