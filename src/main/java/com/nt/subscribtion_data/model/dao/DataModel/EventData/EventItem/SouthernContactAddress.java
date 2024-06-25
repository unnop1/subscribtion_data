package com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class SouthernContactAddress {
    /*
     {
        "building": "A",
        "country": "Thailand",
        "houseNumber": "1",
        "khetAmphur": "เขตหลักสี่",
        "kwangTambon": "แขวงทุ่งสองห้อง",
        "moo": "หมู่ 4",
        "postCode": "11000",
        "province": "กรุงเทพมหานคร",
        "road": "",
        "troksoi": "",
        "village": ""
    }
     */

    @JsonProperty("building")
    private String building;

    @JsonProperty("country")
    private String country;

    @JsonProperty("houseNumber")
    private String houseNumber;

    @JsonProperty("khetAmphur")
    private String khetAmphur;

    @JsonProperty("kwangTambon")
    private String kwangTambon;

    @JsonProperty("moo")
    private String moo;

    @JsonProperty("postCode")
    private String postCode;

    @JsonProperty("province")
    private String province;

    @JsonProperty("road")
    private String road;

    @JsonProperty("troksoi")
    private String troksoi;

    @JsonProperty("village")
    private String village;
}
