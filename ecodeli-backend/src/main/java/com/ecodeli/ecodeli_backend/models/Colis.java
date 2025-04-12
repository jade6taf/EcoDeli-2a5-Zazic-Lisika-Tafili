package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @NotNull(message = "Le poids est obligatoire")
    @Positive(message = "Le poids doit être positif")
    @Column(name = "poid", precision = 10, scale = 2)
    private BigDecimal poid;

    @Positive(message = "La taille doit être positive")
    @Column(name = "taille")
    private Integer taille;

    @Column(name = "type", length = 255)
    private String type;

    @ManyToOne
    @JoinColumn(name = "id_expediteur", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne
    @JoinColumn(name = "id_destinataire", nullable = false)
    private Utilisateur destinataire;
}