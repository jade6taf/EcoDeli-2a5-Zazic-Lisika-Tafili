package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDomicileDetailsDTO {
    
    @NotEmpty(message = "Au moins un type de service doit être sélectionné")
    private List<@Pattern(regexp = "^(menage|garde_enfants|garde_animaux|jardinage|assistance_pa)$", 
                         message = "Type de service invalide") String> typeService;

    @NotBlank(message = "La date souhaitée est obligatoire")
    private String dateSouhaitee;

    @NotBlank(message = "La fréquence est obligatoire")
    @Pattern(regexp = "^(ponctuel|hebdomadaire|mensuel)$", 
             message = "Fréquence invalide")
    private String frequence;

    // Champs complémentaires
    @Pattern(regexp = "^(1h|2h|3h|4h\\+)?$", message = "Durée estimée invalide")
    private String dureeEstimee;

    private String adresseService;
    private String informationsParticulieres;

    private Boolean materielFourni;
    private String contraintesAcces;
    private String preferencesHoraires;

    @Min(value = 1, message = "Le nombre d'enfants doit être au moins 1")
    @Max(value = 10, message = "Le nombre d'enfants ne peut pas dépasser 10")
    private Integer nombreEnfants;

    @Min(value = 1, message = "Le nombre d'animaux doit être au moins 1")
    @Max(value = 10, message = "Le nombre d'animaux ne peut pas dépasser 10")
    private Integer nombreAnimaux;

    private String typeAnimaux;
    private Double surfaceJardin;
}
