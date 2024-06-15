package com.nt.subscribtion_data.model.dao.OMMYFRONT;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListIMSIOfferingConfigClientResp {
    private List<IMSIOfferingConfig> data;
    private String err;
}
