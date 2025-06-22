package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CANDIDATURE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Long idCandidature;

    @NotNull(message = "Le prix proposé est obligatoire")
    @Positive(message = "Le prix proposé doit être positif")
    @Column(name = "prix_propose", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixPropose;

    @Column(name = "message_prestataire", columnDefinition = "TEXT")
    private String messagePrestataire;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(name = "date_candidature", nullable = false)
    private LocalDateTime dateCandidature;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "commentaire_client", columnDefinition = "TEXT")
    private String commentaireClient;

    @Column(name = "delai_propose")
    private Integer delaiPropose;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private DemandeService demandeService;

    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Prestataire prestataire;

    @PrePersist
    protected void onCreate() {
        dateCandidature = LocalDateTime.now();
    }

    public enum StatutCandidature {
        EN_ATTENTE("En attente de réponse"),
        ACCEPTEE("Candidature acceptée"),
        REFUSEE("Candidature refusée");

        private final String libelle;

        StatutCandidature(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean peutEtreAcceptee() {
        return statut == StatutCandidature.EN_ATTENTE &&
               demandeService != null &&
               demandeService.peutRecevoirCandidatures();
    }

    public boolean peutEtreRefusee() {
        return statut == StatutCandidature.EN_ATTENTE;
    }

    public void accepter(String commentaire) {
        this.statut = StatutCandidature.ACCEPTEE;
        this.dateReponse = LocalDateTime.now();
        this.commentaireClient = commentaire;
    }

    public void refuser(String commentaire) {
        this.statut = StatutCandidature.REFUSEE;
        this.dateReponse = LocalDateTime.now();
        this.commentaireClient = commentaire;
    }
}
