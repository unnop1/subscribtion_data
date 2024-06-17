package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopUp {
    /*
     {
        "serialNumber": "",
        "topupType": "",
        "rechargeAmount": "",
        "currencyId": "",
        "channelId": "",
        "notiMsgSeq" : "",
        "rechargeDate" : "",
        "rechargeLogId" : ""
    }
     */
    @JsonProperty("serialNumber")
    private String serialNumber;

    @JsonProperty("topupType")
    private Integer topupType;

    @JsonProperty("rechargeAmount")
    private String rechargeAmount;

    @JsonProperty("rechargeType")
    private Integer rechargeType;
    
    @JsonProperty("currencyId")
    private Integer currencyId;

    @JsonProperty("channelId")
    private Integer channelId;

    @JsonProperty("rechargeDate")
    private String rechargeDate;

    @JsonProperty("notiMsgSeq")
    private String notiMsgSeq;

    @JsonProperty("rechargeLogId")
    private String rechargeLogId;
}
