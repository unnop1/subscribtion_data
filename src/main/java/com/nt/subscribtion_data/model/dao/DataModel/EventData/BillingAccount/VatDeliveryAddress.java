package com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VatDeliveryAddress {
    /*
     {
        "building": "",
        "country": "",
        "houseNumber": "",
        "khetAmphur": "",
        "kwangTambon": "",
        "moo": "",
        "postCode": "",
        "province": "",
        "road": "",
        "troksoi": "",
        "village": ""
    }
     */
    @JsonProperty("title")
    private String title;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("companyTitle")
    private String companyTitle;

    @JsonProperty("company")
    private String company;

    @JsonProperty("building")
    private String building;

    @JsonProperty("country")
    private String country;

    @JsonProperty("houseNumber")
    private String houseNumber;

    @JsonProperty("khetAmphur")
    private String khetAmphur;

    @JsonProperty("kwangTambon")
    private String kwangTambon;

    @JsonProperty("moo")
    private String moo;

    @JsonProperty("postCode")
    private String postCode;

    @JsonProperty("postalCode")
    private String postalCode;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("khet")
    private String khet;

    @JsonProperty("kwang")
    private String kwang;

    @JsonProperty("province")
    private String province;

    @JsonProperty("road")
    private String road;

    @JsonProperty("troksoi")
    private String troksoi;

    @JsonProperty("village")
    private String village;
}
