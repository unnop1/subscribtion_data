package com.nt.subscribtion_data.entity;

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
@Table(name = "CONSUMER_ORDERTYPE", schema = "${replace_schema}")
public class ConsumerOrderTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consumer_ordertype_seq")
    @SequenceGenerator(name = "consumer_ordertype_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long ID;

    @Column(name = "CONSUMER_ID", nullable = true)
    private Long consumer_id=null;

    @Column(name = "ORDERTYPE_ID", nullable = true)
    private Long ordertype_id=null;

    @Column(name = "CREATED_DATE", nullable = true)
    private Timestamp created_date=null;

    @Column(name = "CREATED_BY", nullable = true)
    private String created_by=null;
}
