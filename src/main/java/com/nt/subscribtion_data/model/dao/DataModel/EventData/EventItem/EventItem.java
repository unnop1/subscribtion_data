package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventItem {
    @JsonProperty("itemType")
    private String itemType;

    @JsonProperty("effectiveDate")
    private String effectiveDate;

    @JsonProperty("executionType")
    private String executionType;

    @JsonProperty("sourceEntity")
    private String sourceEntity;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("expire")
    private Expire expire;

    @JsonProperty("offer")
    private List<Offer> offer;

    @JsonProperty("photo")
    private List<Photo> photo;

    // @JsonProperty("destinationCustomerAccount")
    // private DestinationCustomerAccount destinationCustomerAccount;

    @JsonProperty("topUp")
    private TopUp topUp;

    @JsonProperty("creditLimit")
    private CreditLimit creditLimit;

    @JsonProperty("southernContactAddress")
    private SouthernContactAddress southernContactAddress;

    // @JsonProperty("destinationSubscriberInfo")
    // private DestinationSubscriberInfo destinationSubscriberInfo;

    @JsonProperty("varietyServices")
    private List<VarietyService> varietyServices;

    @JsonProperty("balanceTransferInfo")
    private BalanceTransferInfo balanceTransferInfo;

    @JsonProperty("extendExpireInfo")
    private ExtendExpireInfo extendExpireInfo;

    @JsonProperty("contractInfo")
    private ContractInfo contractInfo;
}
