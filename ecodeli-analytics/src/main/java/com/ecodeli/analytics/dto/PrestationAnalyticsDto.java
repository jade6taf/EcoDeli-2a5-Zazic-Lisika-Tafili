package com.ecodeli.analytics.dto;

import com.ecodeli.analytics.enums.ServiceType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrestationAnalyticsDto {
    private Long id;
    private String nom;
    private String description;
    private ServiceType typeService;
    private String typeServiceLibelle;

    private Integer nombreUtilisations;
    private Integer frequenceUtilisation;
    private LocalDateTime datePremiereUtilisation;
    private LocalDateTime dateDerniereUtilisation;

    private Duration dureeMoyenne;
    private Integer dureeMoyenneMinutes;
    private Integer dureeMaxMinutes;
    private Integer dureeMinMinutes;

    private BigDecimal prixUnitaire;
    private BigDecimal chiffreAffairesGenere;
    private BigDecimal recetteMoyenneParUtilisation;

    private Integer ranking;
    private Double tauxSatisfaction;
    private Integer nombreClients;
    private Boolean isPopulaire;

    private String villesPrincipales;
    private String regionPopulaire;

    private String sourceData;
    private LocalDateTime lastUpdate;
    private Boolean isActive;

    public String getDisplayName() {
        return nom + " (" + typeServiceLibelle + ")";
    }

    public String getFrequenceDescription() {
        if (frequenceUtilisation == null || frequenceUtilisation == 0) {
            return "Rarement utilisé";
        } else if (frequenceUtilisation >= 50) {
            return "Très fréquent";
        } else if (frequenceUtilisation >= 20) {
            return "Fréquent";
        } else if (frequenceUtilisation >= 5) {
            return "Modéré";
        } else {
            return "Occasionnel";
        }
    }

    public String getPopulariteLevel() {
        if (ranking == null) return "Non classé";
        if (ranking <= 5) return "Top 5";
        if (ranking <= 10) return "Top 10";
        if (ranking <= 20) return "Populaire";
        return "Standard";
    }

    public String getDureeMoyenneFormatted() {
        if (dureeMoyenneMinutes == null) return "Non défini";
        int heures = dureeMoyenneMinutes / 60;
        int minutes = dureeMoyenneMinutes % 60;

        if (heures > 0) {
            return String.format("%dh%02dm", heures, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    public boolean isPrestationDemandee() {
        return nombreUtilisations != null && nombreUtilisations >= 10;
    }

    public String getCategorieRevenu() {
        if (chiffreAffairesGenere == null) return "Inconnu";

        BigDecimal ca = chiffreAffairesGenere;
        if (ca.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return "Très rentable";
        } else if (ca.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return "Rentable";
        } else if (ca.compareTo(BigDecimal.valueOf(200)) >= 0) {
            return "Modéré";
        } else {
            return "Faible";
        }
    }
}
