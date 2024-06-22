package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import java.time.LocalDateTime;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("contractStart")
    private LocalDateTime contractStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("contractEnd")
    private LocalDateTime contractEnd;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("bypassDate")
    private LocalDateTime bypassDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("requestBypassDate")
    private LocalDateTime requestBypassDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("approveBypassDate")
    private LocalDateTime approveBypassDate;

    @JsonProperty("billRefNo")
    private String billRefNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("billRefDate")
    private LocalDateTime billRefDate;

    @JsonProperty("billRefAmount")
    private Long billRefAmount;
    
    @JsonProperty("manageContractType")
    private Long manageContractType;

    @JsonProperty("remark")
    private String remark;



}
