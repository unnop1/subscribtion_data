package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvDestinationSubscriberInfo {


    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("reserveFlag")
    private Boolean reserveFlag;

    @JsonProperty("preProFlag")
    private Boolean preProFlag;

    @JsonProperty("sourceSimInfo")
    private List<EvDestinationSourceSimInfo> sourceSimInfo;

    @JsonProperty("destinationSimInfo")
    private List<EvDestinationSimInfo> destinationSimInfo;

    @JsonProperty("touristSimFlag")
    private String touristSimFlag;

    @JsonProperty("subscriberNumber")
    private String subscriberNumber;
}
