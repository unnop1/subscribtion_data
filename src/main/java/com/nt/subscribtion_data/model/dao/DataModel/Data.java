package com.nt.subscribtion_data.model.dao.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {
    /*
        "triggerDate": "2024-03-26 02:18:42",
        "publishChannel": "Topup-GW",
        "eventData": {
          "eventType": "Topup/Recharge",
          "eventItem": [
            {
              "topUp": {
                "topupType": "1",
                "rechargeAmount": 500,
                "currencyId": 1141,
                "channelId": 117,
              },
              "destinationSubscriberInfo": {
                "msisdn": "812345678",
              },
            }
          ],
        }
    */
    @JsonProperty("triggerDate")
    private String triggerDate;

    @JsonProperty("publishChannel")
    private String publishChannel;

    @JsonProperty("eventData")
    private EventData eventData;

}
