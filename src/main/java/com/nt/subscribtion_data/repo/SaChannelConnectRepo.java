package com.nt.subscribtion_data.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nt.subscribtion_data.entity.SaChannelConEntity;

public interface SaChannelConnectRepo extends JpaRepository<SaChannelConEntity,Long> {
    @Query(value = "SELECT * FROM sa_channel_connect", nativeQuery = true)
    public List<SaChannelConEntity> ListChannelConnect();
}
