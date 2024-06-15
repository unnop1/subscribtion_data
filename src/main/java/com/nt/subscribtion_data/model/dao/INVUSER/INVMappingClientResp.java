package com.nt.subscribtion_data.model.dao.INVUSER;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class INVMappingClientResp {
    private INVMappingData data;

    private String err;

}
