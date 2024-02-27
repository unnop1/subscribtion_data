package com.nt.subscribtion_data.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.nt.subscribtion_data.model.dao.OMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;

@Component
public class OMService {
    @Autowired
    private JdbcDatabaseService jbdcDB;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; 

    public String getOMQuery(ReceiveOMDataType paramDataType) {
        String tableName = "om_table";

        String query = "SELECT * FROM "+ tableName;      
                    
        System.out.println("query condition : "+query);
        return query;
    }

    public OMDataType getOMDataType(String query) throws SQLException {
        // String query = "SELECT * FROM conditions";
        String databaseName = "admin_red_sms";
        Connection con = jbdcDB.getConnection();
  
        OMDataType orderTypeData = new OMDataType();
        try{
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
  
            
            if (rs.next()) {
                // Retrieve data from the first row
                

            } 
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            
        }
        return orderTypeData;
    }
}
