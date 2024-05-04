package com.nt.subscribtion_data.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;


@Component
public class TopUpService {
    @Autowired
    private JdbcDatabaseService jbdcDB;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; 

    public String getTopUpQuery(ReceiveTopUpDataType paramDataType) {
        String tableName = "topup_table";

        String query = "SELECT * FROM "+ tableName;      
                    
        System.out.println("query condition : "+query);
        return query;
    }

    public Data getTopUpDataType(String query) throws SQLException {
        Connection con = null;
        Data orderTypeData = new Data();
        try {
            con = jbdcDB.getConnection();
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                // Retrieve data from the first row
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Close the connection in the finally block to ensure it's always closed, even if an exception occurs
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    System.out.println("Error while closing connection: " + e.getMessage());
                }
            }
        }
        return orderTypeData;
    }
}
