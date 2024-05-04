package com.nt.subscribtion_data.service;
// Java Program Illustrating Utility class for Connecting
// and Querying the Databas

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


// Class
@Component
public class JdbcDatabaseService {

    @Value("${spring.datasource.url}")
    private String connectionUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.is_cluster_mode}")
    private Boolean isClusterMode;

    private final HikariDataSource dataSource;

    public JdbcDatabaseService(@Value("${spring.datasource.url}") String jdbcUrl,
                               @Value("${spring.datasource.username}") String jdbcUsername,
                               @Value("${spring.datasource.password}") String jdbcPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        if (isClusterMode){
            System.out.println("Not supported");
            return dataSource.getConnection();
        }
        return dataSource.getConnection();
    }
}


