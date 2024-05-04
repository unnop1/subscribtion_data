package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopUp {
    /*
     {
        "serialNumber": "",
        "topupType": "",
        "rechargeAmount": "",
        "currencyId": "",
        "channelId": ""
    }
     */
    @JsonProperty("serialNumber")
    private String serialNumber;

    @JsonProperty("topupType")
    private String topupType;

    @JsonProperty("rechargeAmount")
    private Integer rechargeAmount;
    
    @JsonProperty("currencyId")
    private Integer currencyId;

    @JsonProperty("channelId")
    private Integer channelId;
}
