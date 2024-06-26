package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubPropertyCode {
    @JsonProperty("value")
    private String value;

    @JsonProperty("code")
    private String code;
}
