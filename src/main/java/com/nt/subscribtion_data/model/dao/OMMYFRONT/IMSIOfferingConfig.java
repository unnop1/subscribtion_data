package com.nt.subscribtion_data.model.dao.OMMYFRONT;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IMSIOfferingConfig {
    @JsonProperty("id")
    private int id;

    @JsonProperty("imsi_PREFIX")
    private String imsiPrefix;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("allow4G")
    private String allow4G;

    @JsonProperty("allow5G")
    private String allow5G;
}
