package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COMMERCANT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Commercant extends Utilisateur {
    @NotBlank(message = "Le nom du commerce est obligatoire")
    @Column(name = "nom_commerce", nullable = false)
    private String nomCommerce;

    @Column(name = "siret", length = 14)
    @Pattern(regexp = "^[0-9]{14}$", message = "Le numéro SIRET doit contenir 14 chiffres")
    private String siret;

    @Column(name = "description_commerce", columnDefinition = "TEXT")
    private String descriptionCommerce;

    @Column(name = "horaires_ouverture", columnDefinition = "TEXT")
    private String horairesOuverture;
}
