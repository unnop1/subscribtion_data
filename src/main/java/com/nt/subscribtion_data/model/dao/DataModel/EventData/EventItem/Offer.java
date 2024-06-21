package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offer {
    /*
    {
        "offeringId": "51005156",
        "offeringType": "PO",
        "actionFlag": "None",
        "offeringNameTh": "",
        "offeringNameEn": "",
        "packageId": "",
        "packageName": "",
        "descriptionTh": "",
        "descriptionEn": "",
        "ServiceType": 0,
        "ocsOfferingName": "",
        "rcAmount": "",
        "rcVatAmount": "",
        "period": "",
        "unitPeriod": "",
        "saleStartDate": "",
        "saleEndDate": "",
        "maxDayAfterActiveDate": "",
        "niceNumberFlag": "",
        "niceNumberLevel": "",
        "conTractFlag": "",
        "contractUnitPeriod": "",
        "catEmpFlag": "",
        "catRetireEmpFlag": "",
        "multisimFlag": "N",
        "topupSimFlag": "",
        "touristSimFlag": "",
        "changePoUssdCode": "",
        "addSoUssdCode": "",
        "deleteSoUssdCode": "",
        "frequency": "850",
        "canSwapPoFlag": ""
    }
     */
    @JsonProperty("offeringId")
    private String offeringId;

    @JsonProperty("offeringType")
    private String offeringType;

    @JsonProperty("actionFlag")
    private String actionFlag;

    @JsonProperty("offeringNameTh")
    private String offeringNameTh;

    @JsonProperty("offeringNameEn")
    private String offeringNameEn;

    @JsonProperty("packageId")
    private Long packageId;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("descriptionTh")
    private String descriptionTh;

    @JsonProperty("descriptionEn")
    private String descriptionEn;

    @JsonProperty("ServiceType")
    private Long ServiceType;

    @JsonProperty("ocsOfferingName")
    private String ocsOfferingName;

    @JsonProperty("rcAmount")
    private BigDecimal rcAmount;

    @JsonProperty("rcVatAmount")
    private BigDecimal rcVatAmount;

    @JsonProperty("period")
    private Integer period;

    @JsonProperty("unitPeriod")
    private String unitPeriod;

    @JsonProperty("saleStartDate")
    private LocalDateTime saleStartDate;

    @JsonProperty("saleEndDate")
    private LocalDateTime saleEndDate;

    @JsonProperty("maxDayAfterActiveDate")
    private BigDecimal maxDayAfterActiveDate;

    @JsonProperty("niceNumberFlag")
    private String niceNumberFlag;

    @JsonProperty("niceNumberLevel")
    private BigDecimal niceNumberLevel;

    @JsonProperty("conTractFlag")
    private String conTractFlag;

    @JsonProperty("contractUnitPeriod")
    private BigDecimal contractUnitPeriod;

    @JsonProperty("catEmpFlag")
    private String catEmpFlag;

    @JsonProperty("catRetireEmpFlag")
    private String catRetireEmpFlag;

    @JsonProperty("multisimFlag")
    private String multisimFlag;

    @JsonProperty("topupSimFlag")
    private String topupSimFlag;

    @JsonProperty("touristSimFlag")
    private String touristSimFlag;

    @JsonProperty("changePoUssdCode")
    private String changePoUssdCode;

    @JsonProperty("addSoUssdCode")
    private String addSoUssdCode;

    @JsonProperty("deleteSoUssdCode")
    private String deleteSoUssdCode;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("canSwapPoFlag")
    private String canSwapPoFlag;
}
