package com.ecodeli.ecodeli_backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningStatsResponse {
    
    private Integer prochainsCreneaux;
    
    private Long heuresDisponibles;
    
    private Long heuresOccupees;
    
    private Double tauxOccupation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime prochainCreneau;
    
    private Integer creneauxSemaine;
    
    private Integer creneauxMois;
    
    private Long heuresDisponiblesSemaine;
    
    private Long heuresDisponiblesMois;
    
    // Méthodes utilitaires
    public void calculateTauxOccupation() {
        if (heuresDisponibles != null && heuresOccupees != null) {
            long totalHeures = heuresDisponibles + heuresOccupees;
            if (totalHeures > 0) {
                this.tauxOccupation = (double) heuresOccupees / totalHeures * 100;
                this.tauxOccupation = Math.round(this.tauxOccupation * 10.0) / 10.0; // Arrondi à 1 décimale
            } else {
                this.tauxOccupation = 0.0;
            }
        }
    }
}
