package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    private BillingInfo billingInfo;

    @JsonProperty("billingAddress")
    private BillingAddress billingAddress;

    @JsonProperty("billDeliveryAddress")
    private BillDeliveryAddress billDeliveryAddress;

    @JsonProperty("vatAddress")
    private VatAddress vatAddress;

    @JsonProperty("vatDeliveryAddress")
    private VatDeliveryAddress vatDeliveryAddress;
}
