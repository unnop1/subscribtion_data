package com.nt.subscribtion_data.model.dao.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {
    /*
        "triggerDate": "2024-03-26 02:18:42",
        "publishChannel": "Topup-GW",
        "eventData": {}
    */
    @JsonProperty("triggerDate")
    private String triggerDate;

    @JsonProperty("publishChannel")
    private String publishChannel;

    @JsonProperty("eventData")
    private EventData eventData;

}
