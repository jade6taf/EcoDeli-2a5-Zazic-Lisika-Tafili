package com.ecodeli.ecodeli_backend.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class RailwayDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                // Parser l'URL PostgreSQL Railway
                URI uri = new URI(databaseUrl);
                
                String host = uri.getHost();
                int port = uri.getPort();
                String database = uri.getPath().substring(1); // Enlever le '/' initial
                String userInfo = uri.getUserInfo();
                
                String username = null;
                String password = null;
                
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }
                
                // Construire l'URL JDBC
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                properties.setUrl(jdbcUrl);
                if (username != null) properties.setUsername(username);
                if (password != null) properties.setPassword(password);
                
                System.out.println("Railway URL converted: " + jdbcUrl + " (user: " + username + ")");
                
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing de DATABASE_URL: " + e.getMessage());
                // Fallback vers l'URL originale avec préfixe jdbc:
                properties.setUrl("jdbc:" + databaseUrl);
            }
        } else {
            // Développement local
            properties.setUrl("jdbc:postgresql://localhost:5432/ecodeli");
            properties.setUsername("ecodeli");
            properties.setPassword("password");
        }
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
