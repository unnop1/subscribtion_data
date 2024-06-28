package com.nt.subscribtion_data.model.dao.DataModel.EventData;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo.SubscriberInfo;
import com.nt.subscribtion_data.util.DateTime;

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

    @JsonProperty("sourceCustomerAccount")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SourceCustomerAccount sourceCustomerAccount;

    @JsonProperty("subscriberInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SubscriberInfo subscriberInfo;

    @JsonProperty("saleInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SaleInfo saleInfo;

    @JsonProperty("writtenLanguage")
    private String writtenLanguage;

    @JsonProperty("ivrLanguage")
    private String ivrLanguage;

    @JsonProperty("orderStatus")
    private String orderStatus;

    public void setSubmitedDate(String submitedDate) {
        // Parse the original date string
        OffsetDateTime dateTime = OffsetDateTime.parse(submitedDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).plusDays(7);
        
        // Format the date to the desired format
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_INSTANT);
        
        // Assign the formatted date to the variable
        this.submitedDate = DateTime.addZeroConvertISODate(formattedDate);
    }

    public void setCompletedDate(String completedDate) {
        // Parse the original date string
        OffsetDateTime dateTime = OffsetDateTime.parse(completedDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).plusDays(7);
        
        // Format the date to the desired format
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_INSTANT);
        
        
        // Assign the formatted date to the variable
        this.completedDate = DateTime.addZeroConvertISODate(formattedDate);
    }
}
