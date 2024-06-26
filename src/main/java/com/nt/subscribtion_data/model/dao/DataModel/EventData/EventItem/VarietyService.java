package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VarietyService {
    /*
    {
        "varietyType": "",
        "enabledFlag": ""
    }    
     */
    @JsonProperty("varietyType")
    private String varietyType;

    @JsonProperty("enabledFlag")
    private Integer enabledFlag;
}
