package com.nt.subscribtion_data.model.dao.DataModel.EventData;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo.SubscriberInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventData {
    /*
    "eventData": {
        "refTransId": "MFE20220801162918387160785887",
        "bulkOrderId": "BULK20231115142424882192817372",
        "channel": "MFE",
        "eventType": "Suspend",
        "submitedDate": "2022-09-14T12:45:47.000Z",
        "completedDate": "2022-09-14T12:45:47.000Z",
        "isProvisionRequired": true,
        "rerunRevisionNumber": 0,
        "eventItem": [],
        "subscriberInfo" : {},
        "saleInfo" : {},
        "writtenLanguage" : "01",
        "ivrLanguage": "01",
        "orderStatus": "Completed"
    }
    */
    @JsonProperty("refTransId")
    private String refTransId;

    @JsonProperty("bulkOrderId")
    private String bulkOrderId;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("eventType")
    private String eventType;

    
    @JsonProperty("submitedDate")
    private String submitedDate;

    
    @JsonProperty("completedDate")
    private String completedDate;

    @JsonProperty("isProvisionRequired")
    private Boolean isProvisionRequired;
    
    @JsonProperty("rerunRevisionNumber")
    private Integer rerunRevisionNumber;

    @JsonProperty("eventItem")
    private List<EventItem> eventItems;

    // @JsonProperty("sourceCustomerAccount")
    // private SourceCustomerAccount sourceCustomerAccount;

    @JsonProperty("destinationCustomerAccount")
    private DestinationCustomerAccount destinationCustomerAccount;

    @JsonProperty("subscriberInfo")
    private SubscriberInfo subscriberInfo;

    @JsonProperty("saleInfo")
    private SaleInfo saleInfo;

    @JsonProperty("writtenLanguage")
    private String writtenLanguage;

    @JsonProperty("ivrLanguage")
    private String ivrLanguage;

    @JsonProperty("orderStatus")
    private String orderStatus;
}
