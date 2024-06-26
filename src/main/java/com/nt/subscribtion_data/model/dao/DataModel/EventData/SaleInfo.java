package com.nt.subscribtion_data.model.dao.DataModel.EventData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleInfo {
    /*
     {
        "saleEmpId": "xxx1234",
        "sapCode": "1x23456",
        "dealerCode": "CAT",
        "registerBySellerName": "บริษัท  โทรคมนาคมแห่งชาติ  จำกัด(มหาชน)",
        "saleRole": "12345678",
        "registerProvince": "กรุงเทพมหานคร",
        "territoryName": "ส่วนขายและบริการลูกค้า",
        "saleRepEmpId": "12345678",
        "saleRepSapCode": "1x23456"
    }
     */
    @JsonProperty("saleEmpId")
    private String saleEmpId;

    @JsonProperty("sapCode")
    private String sapCode;

    @JsonProperty("dealerCode")
    private String dealerCode;

    @JsonProperty("registerBySellerName")
    private String registerBySellerName;

    @JsonProperty("saleRole")
    private String saleRole;

    @JsonProperty("registerProvince")
    private String registerProvince;

    @JsonProperty("territoryName")
    private String territoryName;

    @JsonProperty("saleRepEmpId")
    private String saleRepEmpId;

    @JsonProperty("saleRepSapCode")
    private String saleRepSapCode;

    @JsonProperty("verifyIdentity")
    private Boolean verifyIdentity;
}
