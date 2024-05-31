package com.nt.subscribtion_data.model.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveExpiredDataType {
    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("expireOfferInstId")
    private String expireOfferInstId;

    @JsonProperty("expireOfferId")
    private String expireOfferId;

    @JsonProperty("expireOfferName")
    private String expireOfferName;

    @JsonProperty("expireDate")
    private String expireDate;
    
    @JsonProperty("offerPerchaseSeq")
    private String offerPerchaseSeq;

    @JsonProperty("notiMsgSeq")
    private String notiMsgSeq;

    @JsonProperty("poId")
    private Long poId;

    @JsonProperty("poName")
    private String poName;

    @JsonProperty("brandId")
    private Integer brandId;

    @JsonProperty("brandName")
    private String brandName;
}
