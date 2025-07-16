package com.ecodeli.analytics.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientAnalyticsDto {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String typeClient;
    private String ville;
    private String region;

    private BigDecimal chiffreAffaires;
    private String trancheCA;
    private Integer nombreFactures;
    private BigDecimal montantMoyenFacture;

    private Integer nombreCommandes;
    private Integer frequenceCommandes;
    private LocalDateTime datePremiereCommande;
    private LocalDateTime dateDerniereCommande;
    private Double scoreFidelite;
    private Integer ancienneteJours;

    private Integer nombreLivraisons;
    private Integer nombrePrestations;
    private Boolean isActive;

    private String sourceData;
    private LocalDateTime lastUpdate;

    public String getDisplayName() {
        return prenom + " " + nom;
    }

    public String getTrancheCAFromMontant(BigDecimal montant) {
        if (montant.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return "0-100€";
        } else if (montant.compareTo(BigDecimal.valueOf(500)) <= 0) {
            return "101-500€";
        } else {
            return ">500€";
        }
    }

    public boolean isClientFidele() {
        return scoreFidelite != null && scoreFidelite >= 70.0;
    }

    public String getFideliteLevel() {
        if (scoreFidelite == null) return "Non évalué";
        if (scoreFidelite >= 80) return "Très fidèle";
        if (scoreFidelite >= 60) return "Fidèle";
        if (scoreFidelite >= 40) return "Modéré";
        return "Occasionnel";
    }
}
