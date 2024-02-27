package com.nt.subscribtion_data.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveTopUpDataType {
    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("rechargeAmount")
    private Integer rechargeAmount;

    @JsonProperty("channelId")
    private Integer channelId;

    @JsonProperty("rechargeType")
    private Integer rechargeType;

    @JsonProperty("currencyId")
    private Integer currencyId;

    @JsonProperty("rechargeDate")
    private String rechargeDate;
    
    @JsonProperty("notiMsgSeq")
    private String notiMsgSeq;

    @JsonProperty("rechargeLogId")
    private String rechargeLogId;
    
}
