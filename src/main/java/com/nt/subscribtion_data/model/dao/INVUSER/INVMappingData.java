package com.nt.subscribtion_data.model.dao.INVUSER;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class INVMappingData {
    @JsonProperty("mapping_ID")
    private Long mappingId;

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

    @JsonProperty("mapping_STATUS")
    private Long mappingStatus;

    @JsonProperty("multisim_FLAG")
    private Long multisimFlag;

    @JsonProperty("secondary_CODE")
    private Long secondaryCode;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("imsi")
    private String imsi;
}
