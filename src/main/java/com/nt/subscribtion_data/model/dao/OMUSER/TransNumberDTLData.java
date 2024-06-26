package com.nt.subscribtion_data.model.dao.OMUSER;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransNumberDTLData {

    /*
     {
        "created_DATE": 1610611626059,
        "updated_DATE": null,
        "created_BY": "test03",
        "updated_BY": null,
        "is_ACTIVE": "Y",
        "remark": null,
        "iccid": "8966001509188045648",
        "imsi": "520001917746564",
        "msisdn": "863105437",
        "trans_MASTER_ID": "MFE20210114150705964804917633",
        "trans_DTL_ID": 31754,
        "trans_NUMBER_ID": 11650,
        "action_FLAG": null,
        "change_FLAG": null,
        "change_REASON": null,
        "expire_EXISTING": null,
        "expire_NEW": null,
        "reconnect_FLAG": 0,
        "transfer_FLAG": null,
        "trans_GROUP_ID": null
    }
     */
    @JsonProperty("trans_NUMBER_ID")
    private Long transNumberID;

    @JsonProperty("created_DATE")
    private String createdDATE;

    @JsonProperty("updated_DATE")
    private String updatedDATE;

    @JsonProperty("created_BY")
    private String createdBY;

    @JsonProperty("is_ACTIVE")
    private String isACTIVE;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("updated_BY")
    private String updatedBY;

    @JsonProperty("action_FLAG")
    private Integer actionFLAG;

    @JsonProperty("change_FLAG")
    private String changeFLAG;

    @JsonProperty("change_REASON")
    private String changeREASON;

    @JsonProperty("expire_EXISTING")
    private String expireEXISTING;

    @JsonProperty("expire_NEW")
    private String expireNEW;

    @JsonProperty("iccid")
    private String iccid;

    @JsonProperty("imsi")
    private String imsi;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("reconnect_FLAG")
    private Integer reconnectFLAG;
    
    @JsonProperty("trans_MASTER_ID")
    private String transMasterID;

    @JsonProperty("transfer_FLAG")
    private Integer transferFLAG;

    @JsonProperty("trans_DTL_ID")
    private Long transDTLID;

    @JsonProperty("trans_GROUP_ID")
    private Long transGroupID;
    
}
