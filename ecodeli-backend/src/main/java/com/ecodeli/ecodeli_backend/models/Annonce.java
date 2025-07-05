package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Transient
    private Integer idExpediteur;

    @ManyToOne
    @JoinColumn(name = "id_livreur", nullable = true)
    private Livreur livreur;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_colis", referencedColumnName = "id_colis")
    private Colis colis;

    @ManyToOne
    @JoinColumn(name = "id_destinataire", nullable = true)
    private Utilisateur destinataire;

    @Column(name = "livraison_partielle_autorisee")
    private Boolean livraisonPartielleAutorisee = false;

    @ManyToOne
    @JoinColumn(name = "id_livreur_segment1", nullable = true)
    private Livreur livreurSegment1;

    @ManyToOne
    @JoinColumn(name = "id_livreur_segment2", nullable = true)
    private Livreur livreurSegment2;

    @Column(name = "entrepot_intermediaire")
    private String entrepotIntermediaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_segment1")
    private StatutSegment statutSegment1 = StatutSegment.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_segment2")
    private StatutSegment statutSegment2 = StatutSegment.DISPONIBLE;

    public enum TypeAnnonce {
        unique, multiple
    }

    public enum StatutAnnonce {
        PUBLIEE,              // Annonce publiée par un client, visible par les livreurs
        VALIDEE,              // Un livreur a pris en charge l'annonce (livraison complète)
        SEGMENT_1_PRIS,       // Segment 1 pris, en attente du segment 2
        SEGMENT_2_PRIS,       // Segment 2 pris, en attente du segment 1
        SEGMENTS_COMPLETS,    // Les deux segments sont pris, prêt à commencer
        EN_COURS_SEGMENT_1,   // Segment 1 en cours de livraison
        ATTENTE_ENTREPOT,     // Segment 1 terminé, colis en attente à l'entrepôt
        EN_COURS_SEGMENT_2,   // Segment 2 en cours de livraison
        EN_COURS,             // La livraison associée est en cours (livraison complète)
        TERMINEE,             // L'annonce est complétée, la livraison est terminée
        ANNULEE               // L'annonce a été annulée par le client
    }

    public enum StatutSegment {
        DISPONIBLE,           // Segment disponible pour prise en charge
        PRIS,                 // Segment pris par un livreur
        EN_COURS,             // Segment en cours de livraison
        TERMINE               // Segment terminé
    }
}
