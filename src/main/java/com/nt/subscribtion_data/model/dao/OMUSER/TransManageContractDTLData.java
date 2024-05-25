package com.nt.subscribtion_data.model.dao.OMUSER;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransManageContractDTLData {
    @JsonProperty("trans_MANAGE_CONTRACT_ID")
    private Long transManageContractId;

    @JsonProperty("bill_REF_NO")
    private String billRefNo;

    @JsonProperty("bill_REF_DATE")
    private String billRefDate;

    @JsonProperty("bill_REF_AMOUNT")
    private Long billRefAmount;

    @JsonProperty("manage_CONTRACT_TYPE")
    private Long manageContractType;

    @JsonProperty("approve_BYPASS_DATE")
    private String approveBypassDate;

    @JsonProperty("request_BYPASS_DATE")
    private String requestBypassDate;

    @JsonProperty("bypass_DATE")
    private String bypassDate;

    @JsonProperty("bypass_REASON")
    private String bypassReason;

    @JsonProperty("bypass_FEE")
    private Long bypassFee;

    @JsonProperty("bypass_APPROVE_BY")
    private String bypassApproveBy;

    @JsonProperty("contract_END")
    private String contractEnd;

    @JsonProperty("contract_VALUE")
    private Long contractValue;

    @JsonProperty("contract_START")
    private String contractStart;

    @JsonProperty("contract_MONTH")
    private Long contractMonth;

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
