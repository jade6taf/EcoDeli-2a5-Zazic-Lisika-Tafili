package com.ecodeli.analytics.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class ServiceDto {
    private Integer id;
    private String nom;
    private String description;
    private String type;
    private String typeLabel;
    private Double prix;
    private String statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateRealisation;
    private Integer clientId;
    private Integer prestataireId;
    private String clientNom;
    private String prestataireNom;
    private Double evaluation;
    private Integer dureeMinutes;

    public String getTypeDisplay() {
        return typeLabel != null ? typeLabel : type;
    }

    public boolean isTermine() {
        return "TERMINE".equals(statut);
    }
}
