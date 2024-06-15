package com.nt.subscribtion_data.model.dao.OMMYFRONT;

import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderHeaderClientResp {
     private OrderHeaderData data;
    private String err;
}
