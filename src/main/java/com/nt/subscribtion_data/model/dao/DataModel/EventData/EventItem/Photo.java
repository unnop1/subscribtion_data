package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Photo {
    /*
    {
        "photoId": "4556005",
        "photoType": "82",
        "dummyPhotoFlag": true
    }
     */
    @JsonProperty("photoId")
    private String photoId;

    @JsonProperty("photoType")
    private Integer photoType;

    @JsonProperty("dummyPhotoFlag")
    private Boolean dummyPhotoFlag;
}
