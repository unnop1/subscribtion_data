package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingInfo {
    /*
     {
        "collectionUnit": "3063",
        "vat": "1",
        "billingPeriod": "M01",
        "billable": "1",
        "billingGroup": "1101",
        "collectionTreatment": "0",
        "dispatchMethod": "1",
        "emailAddress": "aaa@nt.ntplc.co.th"
    }
     */
    @JsonProperty("collectionUnit")
    private String collectionUnit;

    @JsonProperty("vat")
    private Integer vat;

    @JsonProperty("billingPeriod")
    private String billingPeriod;

    @JsonProperty("billable")
    private Integer billable;

    @JsonProperty("billingGroup")
    private String billingGroup;

    @JsonProperty("collectionTreatment")
    private Integer collectionTreatment;

    @JsonProperty("dispatchMethod")
    private Integer dispatchMethod;

    @JsonProperty("emailAddress")
    private String emailAddress;
}
