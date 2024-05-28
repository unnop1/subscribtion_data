package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Expire {
    /*
     {
        "msisdn":"0864879710",
        "expireOfferInstId":"48176470",
        "expireOfferId":"51005356",
        "expireOfferName":"SO_Prepaid_3G _NetSutKhum _2Mbps. _9Bht (VAT_Excluded) _24hrs.",
        "expireDate":"2024-05-20",
        "offerPerchaseSeq":"48176470",
        "notiMsgSeq":"1750000000023497016",
        "poId":"51005156",
        "poName":"PO_Prepaid_my _Super_Khum",
        "brandId":1,
        "brandName":"CAT"
    }
     */

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
    private String poId;

    @JsonProperty("poName")
    private String poName;

    @JsonProperty("brandId")
    private Integer brandId;

    @JsonProperty("brandName")
    private String brandName;
}
