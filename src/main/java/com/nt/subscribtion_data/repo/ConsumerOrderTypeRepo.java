package com.nt.subscribtion_data.repo;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import com.nt.subscribtion_data.entity.ConsumerOrderTypeEntity;
import com.nt.subscribtion_data.entity.view.consumer_ordertype.ConsumerLJoinOrderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerOrderTypeRepo extends JpaRepository<ConsumerOrderTypeEntity,Long> {

    @Query(value = """
                  SELECT cod.ID, cod.CONSUMER_ID,odt.ORDERTYPE_NAME, cod.ORDERTYPE_ID, con.consumer_group
                FROM consumer_ordertype cod
                LEFT join ordertype odt
                ON cod.ORDERTYPE_ID = odt.ID
                LEFT join consumer con
                ON cod.consumer_id = con.ID
                WHERE cod.ORDERTYPE_ID=?1
                   """,
                 nativeQuery = true)
    public List<ConsumerLJoinOrderType> getAllConsumerOrderType(Long orderTypeID);

}

