package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.DestinationSubscriberInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class EventItem {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("itemType")
    private String itemType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("effectiveDate")
    private String effectiveDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("executionType")
    private String executionType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sourceEntity")
    private String sourceEntity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("userRole")
    private String userRole;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("expire")
    private Expire expire;

    @JsonProperty("offer")
    private List<Offer> offer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("photo")
    private List<Photo> photo;

    // @JsonProperty("destinationCustomerAccount")
    // private DestinationCustomerAccount destinationCustomerAccount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("topUp")
    private TopUp topUp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("creditLimit")
    private CreditLimit creditLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("southernContactAddress")
    private SouthernContactAddress southernContactAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("destinationSubscriberInfo")
    private DestinationSubscriberInfo destinationSubscriberInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("varietyServices")
    private List<VarietyService> varietyServices;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("balanceTransferInfo")
    private BalanceTransferInfo balanceTransferInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("extendExpireInfo")
    private ExtendExpireInfo extendExpireInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("contractInfo")
    private ContractInfo contractInfo;
}
