package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestinationSubscriberInfo {
    @JsonProperty("msisdn")
    private String msisdn;
}
