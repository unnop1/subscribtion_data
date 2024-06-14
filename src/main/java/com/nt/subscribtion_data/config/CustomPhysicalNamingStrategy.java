package com.nt.subscribtion_data.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
    
    @Value("${spring.jpa.schema}")
    private String schemaName;
    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        if (identifier != null) {
            return super.toPhysicalSchemaName(new Identifier(schemaName, false), jdbcEnvironment);
        }
        return super.toPhysicalSchemaName(identifier, jdbcEnvironment);
    }
}