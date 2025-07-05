package com.ecodeli.ecodeli_backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateString = p.getValueAsString();

                try {
                    if (dateString.endsWith("Z")) {
                        return LocalDateTime.parse(dateString.substring(0, dateString.length() - 1));
                    }
                    else if (dateString.contains("T")) {
                        return LocalDateTime.parse(dateString);
                    }
                    else {
                        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                } catch (DateTimeParseException e) {
                    throw new IOException("Impossible de parser la date: " + dateString, e);
                }
            }
        });
        mapper.registerModule(javaTimeModule);
        mapper.registerModule(customModule);
        return mapper;
    }
}
