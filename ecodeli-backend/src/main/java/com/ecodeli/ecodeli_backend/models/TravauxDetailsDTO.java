package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravauxDetailsDTO {
    
    @NotEmpty(message = "Au moins un type de travaux doit être sélectionné")
    private List<@Pattern(regexp = "^(bricolage|plomberie|electricite|peinture|montage|renovation)$", 
                         message = "Type de travaux invalide") String> typeTravaux;

    @NotBlank(message = "La description des travaux est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit faire entre 10 et 1000 caractères")
    private String descriptionTravaux;

    @NotBlank(message = "La date souhaitée est obligatoire")
    private String dateSouhaitee;

    // Champs complémentaires
    @Pattern(regexp = "^(appartement|maison|bureau)?$", 
             message = "Type de logement invalide")
    private String typeLogement;

    @Pattern(regexp = "^(standard|urgent)$", 
             message = "Niveau d'urgence invalide")
    private String urgence = "standard";

    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget doit être positif")
    private BigDecimal budgetEstimatif;

    private String outilsRequis;
    private String materiauxAFournir;
    private String accesBatiment;
    private String contraintesParticulieres;

    private Boolean diagnosticRequis;
    private Boolean devisGratuit;

    @Min(value = 1, message = "La surface doit être au moins 1m²")
    private Double surfaceATravail;

    private String niveauDifficulte;
    private String delaiMaxAcceptable;
}
