package com.nt.subscribtion_data.model.dao.DataModel.EventData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.BillingAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceCustomerAccount {
    /*
    {
        "address": {},
        "billingAccount" : {},
        "cardNumber": "1234567890123",
        "cardType": "1",
        "catEmployeeFlag": "1",
        "companyBranchId": "",
        "companyName": "",
        "companyType": "",
        "contactNumber": "0812345678",
        "custAccountId": "12345678",
        "customerFocus": "",
        "customerGroup": "74",
        "customerId": "123456",
        "customerInfoType": "",
        "customerSegment": "14",
        "customerType": "1",
        "dob": "2008-06-09",
        "documentNumber": "1234567890123",
        "documentType": "1",
        "emailAddress": "",
        "existingFlag": false,
        "firstName": "Bob",
        "gender": "2",
        "ivrLanguage": "01",
        "lastName": "Bob",
        "nationality": "TH",
        "taxRegisterNumber": "1234567890123",
        "title": "74",
        "writtenLanguage": "01"
    }
    */

    @JsonProperty("address")
    private Address address;

    @JsonProperty("billingAccount")
    private BillingAccount billingAccount;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("cardType")
    private Integer cardType;

    @JsonProperty("catEmployeeFlag")
    private Integer catEmployeeFlag;

    @JsonProperty("companyBranchId")
    private String companyBranchId;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("companyType")
    private Integer companyType;

    @JsonProperty("contactNumber")
    private String contactNumber;

    @JsonProperty("custAccountId")
    private String custAccountId;


    @JsonProperty("customerFocus")
    private String customerFocus;

    @JsonProperty("customerGroup")
    private Integer customerGroup;


    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("customerInfoType")
    private Integer customerInfoType;

    @JsonProperty("customerSegment")
    private Integer customerSegment;

    @JsonProperty("customerType")
    private Integer customerType;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("documentNumber")
    private String documentNumber;


    @JsonProperty("documentType")
    private Integer documentType;

    @JsonProperty("emailAddress")
    private String emailAddress;


    @JsonProperty("existingFlag")
    private Boolean existingFlag;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("gender")
    private Integer gender;

    @JsonProperty("ivrLanguage")
    private String ivrLanguage;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("taxRegisterNumber")
    private String taxRegisterNumber;

    @JsonProperty("title")
    private Integer title;

    @JsonProperty("writtenLanguage")
    private String writtenLanguage;

}
