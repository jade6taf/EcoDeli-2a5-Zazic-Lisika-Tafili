package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SERVICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Long idService;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "date_realisation_prevue")
    private LocalDateTime dateRealisationPrevue;

    @NotNull(message = "Le prix final est obligatoire")
    @Positive(message = "Le prix final doit être positif")
    @Column(name = "prix_final", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixFinal;

    @Column(name = "adresse_service", columnDefinition = "TEXT")
    private String adresseService;

    @Column(name = "adresse_secondaire", columnDefinition = "TEXT")
    private String adresseSecondaire;

    @NotNull(message = "Le statut du service est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutService statut = StatutService.CONFIRME;

    @Column(name = "notes_client", columnDefinition = "TEXT")
    private String notesClient;

    @Column(name = "notes_prestataire", columnDefinition = "TEXT")
    private String notesPrestataire;

    @Min(value = 0, message = "La progression doit être comprise entre 0 et 100")
    @Max(value = 100, message = "La progression doit être comprise entre 0 et 100")
    @Column(name = "progression", nullable = false)
    private Integer progression = 0;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @OneToOne
    @JoinColumn(name = "id_demande_service", nullable = false)
    private DemandeService demandeService;

    @OneToOne
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Prestataire prestataire;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum StatutService {
        CONFIRME("Service confirmé - En attente de début"),
        EN_COURS("Service en cours de réalisation"),
        TERMINE("Service terminé"),
        ANNULE("Service annulé");

        private final String libelle;

        StatutService(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean peutCommencer() {
        return statut == StatutService.CONFIRME;
    }

    public boolean estEnCours() {
        return statut == StatutService.EN_COURS;
    }

    public boolean estTermine() {
        return statut == StatutService.TERMINE;
    }

    public boolean peutEtreAnnule() {
        return statut == StatutService.CONFIRME || statut == StatutService.EN_COURS;
    }

    public void commencer() {
        if (peutCommencer()) {
            this.statut = StatutService.EN_COURS;
            this.dateDebut = LocalDateTime.now();
        }
    }

    public void terminer() {
        if (estEnCours()) {
            this.statut = StatutService.TERMINE;
            this.dateFin = LocalDateTime.now();
        }
    }

    public void annuler() {
        if (peutEtreAnnule()) {
            this.statut = StatutService.ANNULE;
        }
    }

    public static Service creerDepuisCandidature(Candidature candidature) {
        Service service = new Service();
        service.setDemandeService(candidature.getDemandeService());
        service.setCandidature(candidature);
        service.setClient((Client) candidature.getDemandeService().getClient());
        service.setPrestataire(candidature.getPrestataire());
        service.setPrixFinal(candidature.getPrixPropose());
        service.setDateRealisationPrevue(candidature.getDemandeService().getDateSouhaitee());
        service.setAdresseService(candidature.getDemandeService().getAdresseDepart());
        service.setAdresseSecondaire(candidature.getDemandeService().getAdresseArrivee());
        return service;
    }
}
