package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ENTREPOT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrepot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrepot")
    private Integer idEntrepot;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutEntrepot statut;

    @Positive(message = "Le nombre de places doit être positif")
    @Column(name = "nombre_de_places")
    private Integer nombreDePlaces;

    @NotBlank(message = "La ville est obligatoire")
    @Column(name = "ville", length = 100)
    private String ville;

    public enum StatutEntrepot {
        ACTIF,
        INACTIF,
        EN_MAINTENANCE,
        COMPLET
    }
}