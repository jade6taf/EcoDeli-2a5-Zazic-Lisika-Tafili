package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "ANNONCE_COMMERCANT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnonceCommercant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_annonce_commercant")
    private Integer idAnnonceCommercant;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false)
    private CategorieAnnonce categorie;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    @Column(name = "titre", length = 200, nullable = false)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le commerçant est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commercant", nullable = false)
    private Commercant commercant;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutAnnonce statut = StatutAnnonce.BROUILLON;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Column(name = "prix_estime", precision = 10, scale = 2)
    private BigDecimal prixEstime;

    // === CATEGORY: LIVRAISON_PONCTUELLE ===
    @Column(name = "date_livraison_precise")
    private LocalDateTime dateLivraisonPrecise;

    @Column(name = "heure_livraison_precise")
    private LocalTime heureLivraisonPrecise;

    @Column(name = "adresse_collecte", columnDefinition = "TEXT")
    private String adresseCollecte;

    @Column(name = "adresse_destination", columnDefinition = "TEXT")
    private String adresseDestination;

    @Column(name = "dimensions_colis")
    private String dimensionsColis;

    @Column(name = "poids_colis", precision = 8, scale = 2)
    private BigDecimal poidsColis;

    @Column(name = "instructions_livraison", columnDefinition = "TEXT")
    private String instructionsLivraison;

    @Column(name = "contact_destinataire")
    private String contactDestinataire;

    @Column(name = "disponibilite_temps_reel")
    private Boolean disponibiliteTempsReel = false;

    // === CATEGORY: SERVICE_CHARIOT ===
    @Column(name = "heures_service_debut")
    private LocalTime heuresServiceDebut;

    @Column(name = "heures_service_fin")
    private LocalTime heuresServiceFin;

    @Column(name = "zone_couverture", columnDefinition = "TEXT")
    private String zoneCouverture;

    @Column(name = "commande_minimum", precision = 10, scale = 2)
    private BigDecimal commandeMinimum;

    @Column(name = "temps_livraison_moyen")
    private Integer tempsLivraisonMoyen; // en minutes

    @Column(name = "creneaux_disponibles", columnDefinition = "TEXT")
    private String creneauxDisponibles; // JSON string

    @Column(name = "jours_service")
    private String joursService; // JSON string pour les jours de la semaine

    @Column(name = "gestion_disponibilite_continue")
    private Boolean gestionDisponibiliteContinue = false;

    // === CATEGORY: TRANSPORT_MARCHANDISES ===
    @Enumerated(EnumType.STRING)
    @Column(name = "frequence_transport")
    private FrequenceTransport frequenceTransport;

    @Column(name = "categories_marchandises", columnDefinition = "TEXT")
    private String categoriesMarchandises; // JSON string

    @Column(name = "conditions_transport", columnDefinition = "TEXT")
    private String conditionsTransport; // température, fragilité, etc.

    @Column(name = "itineraires_detailles", columnDefinition = "TEXT")
    private String itinerairesDetailles; // JSON string

    @Column(name = "volume_estime", precision = 10, scale = 2)
    private BigDecimal volumeEstime;

    @Column(name = "capacite_vehicule_requise")
    private String capaciteVehiculeRequise;

    @Column(name = "optimisation_route")
    private Boolean optimisationRoute = false;

    @Column(name = "planification_capacite")
    private Boolean planificationCapacite = false;

    // === WORKFLOW ===
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_approbation")
    private StatutApprobation statutApprobation = StatutApprobation.EN_ATTENTE;

    @Column(name = "commentaire_approbation", columnDefinition = "TEXT")
    private String commentaireApprobation;

    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_approbateur")
    private Admin adminApprobateur;

    @Column(name = "aide_contextuelle_affichee")
    private Boolean aideContextuelleAffichee = false;

    @Column(name = "etapes_completees", columnDefinition = "TEXT")
    private String etapesCompletees; // JSON string

    public enum StatutAnnonce {
        BROUILLON("Brouillon"),
        EN_ATTENTE_APPROBATION("En attente d'approbation"),
        APPROUVEE("Approuvée"),
        PUBLIEE("Publiée"),
        ACTIVE("Active"),
        SUSPENDUE("Suspendue"),
        EXPIREE("Expirée"),
        ANNULEE("Annulée");

        private final String libelle;

        StatutAnnonce(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutApprobation {
        EN_ATTENTE("En attente"),
        APPROUVEE("Approuvée"),
        REJETEE("Rejetée"),
        REVISION_DEMANDEE("Révision demandée");

        private final String libelle;

        StatutApprobation(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum FrequenceTransport {
        UNIQUE("Une seule fois"),
        HEBDOMADAIRE("Hebdomadaire"),
        MENSUELLE("Mensuelle"),
        REGULIERE("Régulière");

        private final String libelle;

        FrequenceTransport(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean isLivraisonPonctuelle() {
        return this.categorie == CategorieAnnonce.LIVRAISON_PONCTUELLE;
    }

    public boolean isServiceChariot() {
        return this.categorie == CategorieAnnonce.SERVICE_CHARIOT;
    }

    public boolean isTransportMarchandises() {
        return this.categorie == CategorieAnnonce.TRANSPORT_MARCHANDISES;
    }

    public boolean isApprouvee() {
        return this.statutApprobation == StatutApprobation.APPROUVEE;
    }

    public boolean isPubliee() {
        return this.statut == StatutAnnonce.PUBLIEE || this.statut == StatutAnnonce.ACTIVE;
    }

    public boolean isActive() {
        return this.statut == StatutAnnonce.ACTIVE && 
               (this.dateExpiration == null || this.dateExpiration.isAfter(LocalDateTime.now()));
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.statut == StatutAnnonce.PUBLIEE && this.datePublication == null) {
            this.datePublication = LocalDateTime.now();
        }
    }
}