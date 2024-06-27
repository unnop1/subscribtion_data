package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractInfo {
    /*
     {
        "subscrNo": "1234567",
        "contractId": "MY2021021123084052792171222",
        "refDocumentId": "MFE20230323121704226389199827",
        "contractCode": "51006761",
        "contractType": "4",
        "contractDesc": "ทดสอบ",
        "contractMonth": "12",
        "contractStart": "2023-04-23 12:16:38",
        "contractEnd": "2023-04-23 12:16:38",
        "contractValue": "100",
        "bypassBy": "123456",
        "bypassApproveBy": "123456",
        "bypassFee": "1990",
        "bypassReason": "Platinum | ติดสัญญา : 24 เดือน (1,990 บาท/เดือน) : ค่าปรับในกรณียกเลิกสัญญา : 50,000 บาท",
        "bypassDate": "2023-04-23 12:16:38",
        "requestBypassDate": "2023-04-23 12:16:38",
        "approveBypassDate": "2023-04-23 12:16:38",
        "billRefNo": "220217002",
        "billRefDate": "2023-04-23 12:16:38",
        "billRefAmount": "390",
        "manageContractType": "3",
        "remark": "ขอยกเลิกสัญญาเนื่องจากครบสัญญาเบอร์สวยตามบันทึกขออนุมัติ"
    }
     */

    @JsonProperty("subscrNo")
    private String subscrNo;

    @JsonProperty("contractId")
    private String contractId;

    @JsonProperty("refDocumentId")
    private String refDocumentId;

    @JsonProperty("contractCode")
    private Long contractCode;

    @JsonProperty("contractType")
    private Long contractType;

    @JsonProperty("contractDesc")
    private String contractDesc;

    @JsonProperty("contractMonth")
    private Integer contractMonth;

    
    @JsonProperty("contractStart")
    private String contractStart;

    
    @JsonProperty("contractEnd")
    private String contractEnd;

    @JsonProperty("contractValue")
    private Long contractValue;
    

    @JsonProperty("bypassBy")
    private Long bypassBy;

    @JsonProperty("bypassApproveBy")
    private String bypassApproveBy;

    @JsonProperty("bypassFee")
    private Long bypassFee;

    @JsonProperty("bypassReason")
    private String bypassReason;

    
    @JsonProperty("bypassDate")
    private String bypassDate;
    
    
    @JsonProperty("requestBypassDate")
    private String requestBypassDate;

    
    @JsonProperty("approveBypassDate")
    private String approveBypassDate;

    @JsonProperty("billRefNo")
    private String billRefNo;

    
    @JsonProperty("billRefDate")
    private String billRefDate;

    @JsonProperty("billRefAmount")
    private Long billRefAmount;
    
    @JsonProperty("manageContractType")
    private Long manageContractType;

    @JsonProperty("remark")
    private String remark;


    public void setContractStart(String contractStart) {
        // Parse the original date string
        OffsetDateTime dateTime = OffsetDateTime.parse(contractStart, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        // Format the date to the desired format
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_INSTANT);
        
        // Assign the formatted date to the variable
        this.contractStart = formattedDate;
    }


    public void setContractEnd(String contractEnd) {
        // Parse the original date string
        OffsetDateTime dateTime = OffsetDateTime.parse(contractEnd, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        // Format the date to the desired format
        String formattedDate = dateTime.format(DateTimeFormatter.ISO_INSTANT);
        
        // Assign the formatted date to the variable
        this.contractEnd = formattedDate;
    }

}
