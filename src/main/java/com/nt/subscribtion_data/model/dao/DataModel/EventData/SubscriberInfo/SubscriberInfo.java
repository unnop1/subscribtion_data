package com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriberInfo {
    /*
     {
        "msisdn": "812345678",
        "serviceType": "0",
        "sourceSimInfo": [],
        "touristSimFlag": "N",
        "subscriberNumber": "1234567",
        "destinationSimInfo": []
    }
     */
    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("serviceType")
    private Integer serviceType;

    @JsonProperty("reserveFlag")
    private Boolean reserveFlag;

    @JsonProperty("preProFlag")
    private Boolean preProFlag;

    @JsonProperty("sourceSimInfo")
    private List<SourceSimInfo> sourceSimInfo;

    @JsonProperty("destinationSimInfo")
    private List<DestinationSimInfo> destinationSimInfo;

    @JsonProperty("touristSimFlag")
    private String touristSimFlag;

    @JsonProperty("subscriberNumber")
    private String subscriberNumber;
}
