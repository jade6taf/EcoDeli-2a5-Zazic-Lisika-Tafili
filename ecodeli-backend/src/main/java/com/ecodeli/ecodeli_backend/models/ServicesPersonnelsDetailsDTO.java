package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicesPersonnelsDetailsDTO {
    
    @NotBlank(message = "Le type de service est obligatoire")
    @Pattern(regexp = "^(administratif|conciergerie|evenementiel|secretariat|redaction)$", 
             message = "Type de service invalide")
    private String typeService;

    @NotBlank(message = "La description de la mission est obligatoire")
    @Size(min = 10, max = 2000, message = "La description doit faire entre 10 et 2000 caractères")
    private String descriptionBesoin;

    @NotBlank(message = "La date souhaitée est obligatoire")
    private String dateSouhaitee;

    // Champs complémentaires
    @Pattern(regexp = "^(1-2h|demi-journee|journee|plusieurs-jours)?$", 
             message = "Durée estimée invalide")
    private String dureeEstimee;

    private String lieuService;
    private String livrables;
    private String delaiReduction;

    private Boolean urgence;
    private Boolean confidentialite;
    private Boolean competencesSpecifiques;

    private String logicielsRequis;
    private String languesRequises;
    private String niveauExpertise;

    private String modalitesContact;
    private String frequenceSupports;
    private String formatLivrable;

    // Pour événementiel
    private Integer nombreInvites;
    private String typeEvenement;
    private String budget;

    // Pour administratif
    private String typeDocuments;
    private Boolean attestationRequise;
}
