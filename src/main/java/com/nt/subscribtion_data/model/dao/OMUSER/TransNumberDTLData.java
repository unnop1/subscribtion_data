package com.nt.subscribtion_data.model.dao.OMUSER;

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
    @Column(name = "trans_NUMBER_ID")
    private Long transNumberID;

    @Column(name = "created_DATE", nullable = true)
    private String createdDATE=null;

    @Column(name = "updated_DATE", nullable = true)
    private String updatedDATE=null;

    @Column(name = "created_BY", nullable = true)
    private String createdBY=null;

    @Column(name = "is_ACTIVE", nullable = true)
    private String isACTIVE=null;

    @Column(name = "remark", nullable = true)
    private String remark=null;

    @Column(name = "updated_BY", nullable = true)
    private String updatedBY=null;

    @Column(name = "action_FLAG", nullable = true)
    private Integer actionFLAG=null;

    @Column(name = "change_FLAG", nullable = true)
    private String changeFLAG=null;

    @Column(name = "change_REASON", nullable = true)
    private String changeREASON=null;

    @Column(name = "expire_EXISTING", nullable = true)
    private String expireEXISTING=null;

    @Column(name = "expire_NEW", nullable = true)
    private String expireNEW=null;

    @Column(name = "iccid", nullable = true)
    private String iccid=null;

    @Column(name = "imsi", nullable = true)
    private String imsi=null;

    @Column(name = "msisdn", nullable = true)
    private String msisdn=null;

    @Column(name = "reconnect_FLAG", nullable = true)
    private Integer reconnectFLAG=null;
    
    @Column(name = "trans_MASTER_ID", nullable = true)
    private String transMasterID=null;

    @Column(name = "transferFLAG", nullable = true)
    private Integer transferFLAG=null;

    @Column(name = "trans_DTL_ID", nullable = true)
    private Long transDTLID=null;

    @Column(name = "trans_GROUP_ID", nullable = true)
    private Long transGroupID=null;
    
}
