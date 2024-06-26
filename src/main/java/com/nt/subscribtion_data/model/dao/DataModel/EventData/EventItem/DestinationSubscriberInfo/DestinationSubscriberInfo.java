package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DestinationSubscriberInfo {

    @JsonProperty("destinationSimInfo")
    private List<DestinationSubscriberInfoSimInfo> destinationSimInfo;
}
