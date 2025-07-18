package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "LIVRAISON")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livraison")
    private Integer idLivraison;

    @ManyToOne
    @JoinColumn(name = "id_annonce")
    private Annonce annonce;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutLivraison statut;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Column(name = "adresse_de_livraison", length = 100)
    private String adresseDeLivraison;

    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    @Column(name = "code_postal_livraison", length = 10)
    private String codePostalLivraison;

    @NotBlank(message = "L'adresse d'envoi est obligatoire")
    @Column(name = "adresse_envoi", length = 100)
    private String adresseEnvoi;

    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    @Column(name = "code_postal_envoi", length = 10)
    private String codePostalEnvoi;

    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    @Column(name = "prix")
    private Integer prix;

    @Size(min = 6, max = 10, message = "Le code de validation doit contenir entre 6 et 10 caractères")
    @Column(name = "code_validation", length = 10)
    private String codeValidation;

    @Column(name = "validation")
    private Boolean validation = false;

    @ManyToOne
    @JoinColumn(name = "id_expediteur", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne
    @JoinColumn(name = "id_destinataire")
    private Utilisateur destinataire;

    @ManyToOne
    @JoinColumn(name = "id_colis")
    private Colis colis;

    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "otp_timestamp")
    private LocalDateTime otpTimestamp;

    @Column(name = "latitude_envoi")
    private Double latitudeEnvoi;

    @Column(name = "longitude_envoi")
    private Double longitudeEnvoi;

    @Column(name = "latitude_livraison")
    private Double latitudeLivraison;

    @Column(name = "longitude_livraison")
    private Double longitudeLivraison;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_livraison")
    private TypeLivraison typeLivraison = TypeLivraison.DIRECTE;

    @ManyToOne
    @JoinColumn(name = "livreur_segment_1")
    private Livreur livreurSegment1;

    @ManyToOne
    @JoinColumn(name = "livreur_segment_2")
    private Livreur livreurSegment2;

    @Column(name = "entrepot_ville", length = 50)
    private String entrepotVille;

    @Column(name = "date_depot_entrepot")
    private LocalDateTime dateDepotEntrepot;

    @Column(name = "date_collecte_entrepot")
    private LocalDateTime dateCollecteEntrepot;

    public enum TypeLivraison {
        DIRECTE,      // Livraison classique en une fois
        PARTIELLE     // Livraison en deux segments
    }

    public enum StatutLivraison {
        VALIDEE,               // Annonce acceptée, en attente de démarrage par le livreur
        EN_COURS,              // Le livreur a démarré la livraison (directe ou segment 1)
        ATTENTE_SEGMENT_2,     // Segment 1 terminé, colis en entrepôt, attente livreur 2
        SEGMENT_2_EN_COURS,    // Segment 2 en cours
        ARRIVED,               // Le livreur est arrivé au point de livraison final, OTP envoyé
        TERMINEE,              // Livraison terminée après validation OTP
        ANNULEE                // Livraison annulée
    }
}
