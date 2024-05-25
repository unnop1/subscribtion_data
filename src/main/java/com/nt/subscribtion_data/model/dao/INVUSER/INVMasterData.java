package com.nt.subscribtion_data.model.dao.INVUSER;

import com.fasterxml.jackson.annotation.JsonProperty;

public class INVMasterData {
    @JsonProperty("created_DATE")
    private String createdDate;

    @JsonProperty("updated_DATE")
    private String updatedDate;

    @JsonProperty("created_BY")
    private String createdBy;

    @JsonProperty("updated_BY")
    private String updatedBy;

    @JsonProperty("is_ACTIVE")
    private String isActive;

    @JsonProperty("external_ID")
    private String externalId;

    @JsonProperty("secondary_CODE")
    private Double secondaryCode;

    @JsonProperty("sim_CODE")
    private String simCode;

    @JsonProperty("extn_ID_TYPE")
    private Long extnIdType;

    @JsonProperty("number_TYPE")
    private Long numberType;

    @JsonProperty("sim_FLAG")
    private Long simFlag;

    @JsonProperty("operator_ID")
    private Long operatorId;

    @JsonProperty("master_ID")
    private Long masterId;

    @JsonProperty("digit")
    private Long digit;

    @JsonProperty("mvno_ID")
    private Long mvnoId;

    @JsonProperty("pin1")
    private Long pin1;

    @JsonProperty("pin2")
    private Long pin2;

    @JsonProperty("puk1")
    private Long puk1;

    @JsonProperty("puk2")
    private Long puk2;

    @JsonProperty("zone_ID")
    private Long zoneId;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("imsi")
    private String imsi;
}
