package com.nt.subscribtion_data.model.dao.OMMYFRONT;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHeaderData {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("createby")
    private String createBy;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    @JsonProperty("createdate")
    private String createDate;

    
    @JsonProperty("currentbalance")
    private String currentBalance;

    @JsonProperty("input_DATA")
    private String inputData;

    @JsonProperty("isfromfuturequeue")
    private String isFromFutureQueue;

    @JsonProperty("isprovisionrequired")
    private String isProvisionRequired;

    @JsonProperty("iswomack")
    private String isWomack;

    @JsonProperty("lastbalance")
    private String lastBalance;

    @JsonProperty("orderexecutiontype")
    private String orderExecutionType;

    @JsonProperty("orderstatus")
    private String orderStatus;

    @JsonProperty("orderstatusdesc")
    private String orderStatusDesc;

    @JsonProperty("ordertype")
    private String orderType;

    @JsonProperty("revisionnumber")
    private Long revisionNumber;

    @JsonProperty("servicetype")
    private Integer serviceType;

    @JsonProperty("updateby")
    private String updateBy;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    @JsonProperty("updatedate")
    private String updateDate;

    @JsonProperty("operating_PART")
    private String operatingPart;

    @JsonProperty("ba")
    private String ba;

    @JsonProperty("ca")
    private String ca;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("iccid")
    private String iccid;

    @JsonProperty("imsi")
    private String imsi;

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("orderid")
    private String orderId;

    @JsonProperty("param1")
    private String param1;

    @JsonProperty("param10")
    private String param10;

    @JsonProperty("param11")
    private String param11;

    @JsonProperty("param12")
    private String param12;

    @JsonProperty("param13")
    private String param13;

    @JsonProperty("param14")
    private String param14;

    @JsonProperty("param15")
    private String param15;

    @JsonProperty("param16")
    private String param16;

    @JsonProperty("param17")
    private String param17;

    @JsonProperty("param18")
    private String param18;

    @JsonProperty("param19")
    private String param19;

    @JsonProperty("param2")
    private String param2;

    @JsonProperty("param20")
    private String param20;

    @JsonProperty("param3")
    private String param3;

    @JsonProperty("param4")
    private String param4;

    @JsonProperty("param5")
    private String param5;

    @JsonProperty("param6")
    private String param6;

    @JsonProperty("param7")
    private String param7;

    @JsonProperty("param8")
    private String param8;

    @JsonProperty("param9")
    private String param9;

    @JsonProperty("poid")
    private Long poid;

    @JsonProperty("soid")
    private String soid;

    @JsonProperty("bulkid")
    private String bulkId;
}
