package com.ecodeli.ecodeli_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "DEMANDE_SERVICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    @Column(name = "titre", nullable = false, length = 100)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "La catégorie de service est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_service", nullable = false)
    private ServiceType categorieService;

    @Column(name = "type_service_specifique", length = 100)
    private String typeServiceSpecifique;

    @Column(name = "service_personnalise")
    private Boolean servicePersonnalise = false;

    @Column(name = "adresse_depart", columnDefinition = "TEXT")
    private String adresseDepart;

    @Column(name = "adresse_arrivee", columnDefinition = "TEXT")
    private String adresseArrivee;

    @Column(name = "date_souhaitee")
    private LocalDateTime dateSouhaitee;

    @Column(name = "creneau_horaire", length = 100)
    private String creneauHoraire;

    @Column(name = "budget_min", precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 10, scale = 2)
    private BigDecimal budgetMax;

    @ElementCollection
    @CollectionTable(name = "demande_service_photos",
                    joinColumns = @JoinColumn(name = "id_demande"))
    @Column(name = "photo_url")
    private List<String> photosUrls = new ArrayList<>();

    @Column(name = "details_specifiques", columnDefinition = "JSON")
    private String detailsSpecifiques;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutDemande statut = StatutDemande.PUBLIEE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @JsonIgnore
    @OneToMany(mappedBy = "demandeService", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Candidature> candidatures = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum StatutDemande {
        PUBLIEE("Publiée - En attente de candidatures"),
        CANDIDATURES_RECUES("Candidatures reçues"),
        PRESTATAIRE_SELECTIONNE("Prestataire sélectionné"),
        EN_COURS("Service en cours"),
        TERMINEE("Service terminé"),
        ANNULEE("Demande annulée");

        private final String libelle;

        StatutDemande(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean peutRecevoirCandidatures() {
        return statut == StatutDemande.PUBLIEE || statut == StatutDemande.CANDIDATURES_RECUES;
    }

    public boolean peutEtreModifiee() {
        return statut == StatutDemande.PUBLIEE || statut == StatutDemande.CANDIDATURES_RECUES;
    }

    public boolean peutEtreAnnulee() {
        return statut != StatutDemande.TERMINEE && statut != StatutDemande.ANNULEE;
    }

    public int getNombreCandidatures() {
        return candidatures != null ? candidatures.size() : 0;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getDetailsSpecifiquesAsMap() {
        if (detailsSpecifiques == null || detailsSpecifiques.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(detailsSpecifiques, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    public void setDetailsSpecifiquesFromMap(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            this.detailsSpecifiques = null;
            return;
        }
        try {
            this.detailsSpecifiques = objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            this.detailsSpecifiques = null;
        }
    }

    public boolean hasDetailsSpecifiques() {
        return detailsSpecifiques != null && !detailsSpecifiques.trim().isEmpty();
    }
}
