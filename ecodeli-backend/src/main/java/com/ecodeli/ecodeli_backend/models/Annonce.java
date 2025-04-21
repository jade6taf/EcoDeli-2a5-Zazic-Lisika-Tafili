package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ANNONCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annonce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_annonce")
    private Integer idAnnonce;

    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    @Column(name = "titre", length = 100)
    private String titre;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Column(name = "prix_unitaire", precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_annonce")
    private TypeAnnonce typeAnnonce = TypeAnnonce.unique;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutAnnonce statut = StatutAnnonce.PUBLIEE;

    @Column(name = "adresse_depart", columnDefinition = "TEXT")
    private String adresseDepart;

    @Column(name = "adresse_fin", columnDefinition = "TEXT")
    private String adresseFin;

    @ManyToOne
    @JoinColumn(name = "id_expediteur", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne
    @JoinColumn(name = "id_livreur", nullable = true)
    private Livreur livreur;

    public enum TypeAnnonce {
        unique, multiple
    }

    public enum StatutAnnonce {
        PUBLIEE,              // Annonce publiée par un client, visible par les livreurs
        VALIDEE,              // Un livreur a pris en charge l'annonce
        EN_COURS,             // La livraison associée est en cours
        TERMINEE,             // L'annonce est complétée, la livraison est terminée
        ANNULEE               // L'annonce a été annulée par le client
    }
}