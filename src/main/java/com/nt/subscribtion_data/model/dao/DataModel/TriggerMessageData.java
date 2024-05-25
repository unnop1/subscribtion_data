package com.nt.subscribtion_data.model.dao.DataModel;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriggerMessageData {
        private Long id = null;
        private Long OrderType_id = null;
        private String OrderType_Name = null;
        private Long SA_CHANNEL_CONNECT_ID = null;
        private String PUBLISH_CHANNEL = null;
        private String PHONENUMBER = null;
        private Integer IS_STATUS = null;
        private String MESSAGE_IN = null;
        private String DATE_MODEL = null;
        private Timestamp RECEIVE_DATE = null;
        private Timestamp SEND_DATE = null;
        private String ORDERID = null;
    
}
