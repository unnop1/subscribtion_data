package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendExpireInfo {
    /*
     {
        "extendedDay": "",
        "balanceAmount": ""
    }
     */
    @JsonProperty("extendedDay")
    private String extendedDay;

    @JsonProperty("balanceAmount")
    private String balanceAmount;
}
