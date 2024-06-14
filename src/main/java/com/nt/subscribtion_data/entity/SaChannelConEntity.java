package com.nt.subscribtion_data.entity;



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
@Table (name = "SA_CHANNEL_CONNECT", schema="${replace_schema}")
public class SaChannelConEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sa_channel_connect_seq")
    @SequenceGenerator(name = "sa_channel_connect_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long ID = null;

    @Column(name = "CHANNEL_NAME", unique = false,nullable = true)
    private String CHANNEL_Name = null;

}
