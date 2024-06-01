package com.nt.subscribtion_data.repo;


import java.util.List;
import org.springframework.data.jpa.repository.Query;
import com.nt.subscribtion_data.entity.OrderTypeEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepo extends JpaRepository<OrderTypeEntity,Long> {

    @Query(value = "SELECT * FROM ordertype", nativeQuery = true)
    public List<OrderTypeEntity> ListOrderType();

}
