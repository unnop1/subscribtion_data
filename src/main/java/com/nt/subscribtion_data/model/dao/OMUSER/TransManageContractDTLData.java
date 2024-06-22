package com.nt.subscribtion_data.model.dao.OMUSER;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransManageContractDTLData {
    @JsonProperty("trans_MANAGE_CONTRACT_ID")
    private Long transManageContractId;

    @JsonProperty("bill_REF_NO")
    private String billRefNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("bill_REF_DATE")
    private LocalDateTime billRefDate;

    @JsonProperty("bill_REF_AMOUNT")
    private Long billRefAmount;

    @JsonProperty("manage_CONTRACT_TYPE")
    private Long manageContractType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("approve_BYPASS_DATE")
    private LocalDateTime approveBypassDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("request_BYPASS_DATE")
    private LocalDateTime requestBypassDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("bypass_DATE")
    private LocalDateTime bypassDate;

    @JsonProperty("bypass_REASON")
    private String bypassReason;

    @JsonProperty("bypass_FEE")
    private Long bypassFee;

    @JsonProperty("bypass_APPROVE_BY")
    private String bypassApproveBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("contract_END")
    private LocalDateTime contractEnd;

    @JsonProperty("contract_VALUE")
    private Long contractValue;

    @JsonProperty("contract_START")
    private LocalDateTime contractStart;

    @JsonProperty("contract_MONTH")
    private Integer contractMonth;

    @JsonProperty("contract_TYPE")
    private Long contractType;

    @JsonProperty("contract_DESC")
    private String contractDesc;

    @JsonProperty("contract_CODE")
    private Long contractCode;

    @JsonProperty("ref_DOCUMENT_ID")
    private String refDocumentId;

    @JsonProperty("contract_ID")
    private String contractId;

    @JsonProperty("updated_DATE")
    private String updatedDate;

    @JsonProperty("is_ACTIVE")
    private String isActive;

    @JsonProperty("subscr_NO")
    private String subscrNo;

    @JsonProperty("updated_BY")
    private String updatedBy;

    @JsonProperty("trans_DTL_ID")
    private Long transDtlId;

    @JsonProperty("created_BY")
    private String createdBy;

    @JsonProperty("created_DATE")
    private String createdDate;

    @JsonProperty("trans_MASTER_ID")
    private String transMasterId;

    @JsonProperty("bypass_BY")
    private Long bypassBy;

    @JsonProperty("enable")
    private String enable;

    @JsonProperty("remark")
    private String remark;
}
