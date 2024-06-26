package com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceSimInfo {
    /*
     {
        "iccid": "8966001234567890123",
        "imsi": "520001234567890",
        "simType": "Primary",
        "frequency": "700"
    }
     */
    @JsonProperty("iccid")
    private String iccid;

    @JsonProperty("imsi")
    private String imsi;

    @JsonProperty("simType")
    private String simType;

    @JsonProperty("frequency")
    private String frequency;
}
