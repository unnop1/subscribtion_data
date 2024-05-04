package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceTransferInfo {
    /*
     {
        "transferTotalFlag": "",
        "transferType": "",
        "transferAmount": ""
    }
     */
    @JsonProperty("transferTotalFlag")
    private String transferTotalFlag;

    @JsonProperty("transferType")
    private String transferType;

    @JsonProperty("transferAmount")
    private String transferAmount;
}
