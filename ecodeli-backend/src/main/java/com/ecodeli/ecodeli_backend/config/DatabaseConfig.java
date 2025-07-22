package com.ecodeli.ecodeli_backend.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // Gérer la conversion d'URL Railway postgresql:// vers jdbc:postgresql://
        String url = System.getenv("DATABASE_URL");
        if (url != null && url.startsWith("postgresql://")) {
            url = "jdbc:" + url;
        } else if (url == null) {
            // Valeur par défaut pour le développement local
            url = "jdbc:postgresql://localhost:5432/ecodeli";
        }
        
        properties.setUrl(url);
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
