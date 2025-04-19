package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PRESTATAIRE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Prestataire extends Utilisateur {
    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Column(name = "nom_entreprise", nullable = false)
    private String nomEntreprise;

    @Column(name = "siret", length = 14)
    @Pattern(regexp = "^[0-9]{14}$", message = "Le numéro SIRET doit contenir 14 chiffres")
    private String siret;

    @Column(name = "domaine_expertise")
    private String domaineExpertise;

    @Column(name = "zone_intervention", columnDefinition = "TEXT")
    private String zoneIntervention;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "tarif_horaire")
    private Double tarifHoraire;
}
