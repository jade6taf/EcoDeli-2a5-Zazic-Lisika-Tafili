package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "COLIS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Colis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_colis")
    private Integer idColis;

    @Positive(message = "Le poids doit être positif")
    @Column(name = "poids")
    private BigDecimal poids;

    @Positive(message = "La longueur doit être positive")
    @Column(name = "longueur")
    private BigDecimal longueur;

    @Positive(message = "La largeur doit être positive")
    @Column(name = "largeur")
    private BigDecimal largeur;

    @Positive(message = "La hauteur doit être positive")
    @Column(name = "hauteur")
    private BigDecimal hauteur;

    @Column(name = "fragile")
    private Boolean fragile = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}