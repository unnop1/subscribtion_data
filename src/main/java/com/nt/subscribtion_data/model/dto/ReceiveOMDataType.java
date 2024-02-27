package com.nt.subscribtion_data.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiveOMDataType {
    @JsonProperty("orderType")
    private String orderType;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("orderId")
    private String orderId;
}
