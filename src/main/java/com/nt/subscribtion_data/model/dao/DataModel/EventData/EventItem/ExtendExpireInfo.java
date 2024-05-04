package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
