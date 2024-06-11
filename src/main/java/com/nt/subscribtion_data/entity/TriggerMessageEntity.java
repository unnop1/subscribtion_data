package com.nt.subscribtion_data.entity;


import java.sql.Clob;
import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table (name = "trigger_message")
public class TriggerMessageEntity {
    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trigger_message_seq")
        @SequenceGenerator(name = "trigger_message_seq", allocationSize = 1)
        @Column(name = "ID")
        private Long id = null;

        @Column(name = "OrderType_id", unique = false,nullable = true)
        private Long OrderType_id = null;

        @Column(name = "OrderType_Name", unique = false,nullable = true)
        private String OrderType_Name = null;

        @Column(name = "SA_CHANNEL_CONNECT_ID", unique = false,nullable = true)
        private Long SA_CHANNEL_CONNECT_ID = null;


        @Column(name = "PUBLISH_CHANNEL", unique = false,nullable = true)
        private String PUBLISH_CHANNEL = null;

        @Column(name = "PHONENUMBER", unique = false,nullable = true)
        private String PHONENUMBER = null;

        @Column(name = "IS_STATUS", unique = false,nullable = true)
        private Integer IS_STATUS = null;

        @Column(name = "MESSAGE_IN", unique = false,nullable = true)
        private Clob MESSAGE_IN = null;

        @Column(name = "DATE_MODEL", unique = false,nullable = true)
        private Clob DATE_MODEL = null;

        @Column(name = "RECEIVE_DATE", unique = false,nullable = true)
        private Timestamp RECEIVE_DATE = null;

        @Column(name = "SEND_DATE", unique = false,nullable = true)
        private Timestamp SEND_DATE = null;

        @Column(name = "ORDERID", unique = false,nullable = true)
        private String ORDERID = null;

}
