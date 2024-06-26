package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.EvDestinationSubscriberInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventItem {
    @JsonProperty("itemType")
    private String itemType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    @JsonProperty("effectiveDate")
    private String effectiveDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    @JsonProperty("executionType")
    private String executionType;

    @JsonProperty("extendedDay")
    private Integer extendedDay;

    @JsonProperty("effectiveMode")
    private String effectiveMode;

    @JsonProperty("orderItemId")
    private String orderItemId;

    @JsonProperty("orderItemStatus")
    private String orderItemStatus;

    @JsonProperty("effectiveTime")
    private String effectiveTime;

    @JsonProperty("actionType")
    private String actionType;

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

    @JsonProperty("destinationSubscriberInfo")
    private EvDestinationSubscriberInfo destinationSubscriberInfo;

    @JsonProperty("topUp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private TopUp topUp;

    @JsonProperty("creditLimit")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CreditLimit creditLimit;

    @JsonProperty("southernContactAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SouthernContactAddress southernContactAddress;

    @JsonProperty("unitType")
    private String unitType;
    
    @JsonProperty("varietyServices")
    private List<VarietyService> varietyServices;

    @JsonProperty("balanceTransferInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BalanceTransferInfo balanceTransferInfo;

    @JsonProperty("extendExpireInfo")
    private ExtendExpireInfo extendExpireInfo;

    @JsonProperty("contractInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ContractInfo contractInfo;
}
