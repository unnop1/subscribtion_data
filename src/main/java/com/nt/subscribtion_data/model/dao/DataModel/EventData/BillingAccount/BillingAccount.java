package com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingAccount {
    /*
     {
        "existingFlag": false,
        "billingAccountId": "",
        "paymentProfile": "",
        "billingInfo": {},
        "billingAddress": {},
        "billDeliveryAddress": {},
        "vatAddress": {},
        "vatDeliveryAddress": {}
    }
     */
    @JsonProperty("existingFlag")
    private Boolean existingFlag;

    @JsonProperty("billingAccountId")
    private String billingAccountId;

    @JsonProperty("paymentProfile")
    private String paymentProfile;

    @JsonProperty("billingInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BillingInfo billingInfo;

    @JsonProperty("billingAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BillingAddress billingAddress;

    @JsonProperty("billDeliveryAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BillDeliveryAddress billDeliveryAddress;

    @JsonProperty("vatAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private VatAddress vatAddress;

    @JsonProperty("vatDeliveryAddress")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private VatDeliveryAddress vatDeliveryAddress;
}
