package com.nt.subscribtion_data.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
public interface TriggerRepo extends JpaRepository<TriggerMessageEntity,Long> {

}