package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportDetailsDTO {
    
    @NotBlank(message = "Le lieu de départ est obligatoire")
    private String lieuDepart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    private String lieuArrivee;

    @NotBlank(message = "La date et heure sont obligatoires")
    private String dateHeure;

    @NotBlank(message = "Le type de transport est obligatoire")
    @Pattern(regexp = "^(personnes|colis|demenagement)$", 
             message = "Type de transport invalide")
    private String typeTransport;

    // Champs complémentaires
    private String descriptionContenu;
    private String instructions;
    private String vehiculeRequis;
    private Integer nombrePersonnes;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le poids doit être positif")
    private Double poidsApproximatif;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le volume doit être positif")
    private Double volumeApproximatif;
}
