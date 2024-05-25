package com.nt.subscribtion_data.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.model.dao.DataModel.TriggerMessageData;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;

@Component
public class DistributeService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; 

    public TriggerMessageData CreateTriggerMessage(TriggerMessageData triggerMessageData) throws SQLException {
        String tableName = "trigger_message";
        String insertQuery = "INSERT INTO " + tableName + " ( OrderType_id, OrderType_Name, SA_CHANNEL_CONNECT_ID, PUBLISH_CHANNEL, PHONENUMBER, IS_STATUS, MESSAGE_IN, DATE_MODEL, RECEIVE_DATE, SEND_DATE, ORDERID  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection con = JdbcDatabaseService.getConnection();
        ) {
            String returnCols[] = { "ID" };
            PreparedStatement statement = con.prepareStatement(insertQuery, returnCols);
            statement.setObject(1, triggerMessageData.getId());
            statement.setObject(1, triggerMessageData.getOrderType_id());
            statement.setObject(2, triggerMessageData.getOrderType_Name());
            statement.setObject(3, triggerMessageData.getSA_CHANNEL_CONNECT_ID());
            statement.setObject(4, triggerMessageData.getPUBLISH_CHANNEL());
            statement.setObject(5, triggerMessageData.getPHONENUMBER());
            statement.setObject(6, triggerMessageData.getIS_STATUS());
            statement.setObject(7, triggerMessageData.getMESSAGE_IN());
            statement.setObject(8, triggerMessageData.getDATE_MODEL());
            statement.setObject(9, triggerMessageData.getRECEIVE_DATE());
            statement.setObject(10, triggerMessageData.getSEND_DATE());
            statement.setObject(11, triggerMessageData.getORDERID());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                    long tid = generatedKeys.getLong(1); // Attempt to retrieve the generated key
                    if (!generatedKeys.wasNull()) { // Check if the key retrieved is not null
                        triggerMessageData.setId(tid); // Set the ID in smsGatewayData
                    } else {
                        throw new SQLException("Creating triggerMessageData failed, no ID obtained.");
                    }
                    } else {
                        throw new SQLException("Creating triggerMessageData failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error createTriggerMessageData: " + e.getMessage());
            throw e; // Rethrow the exception to propagate it
        }

        return triggerMessageData;
    }

    
}
