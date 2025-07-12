package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "CANDIDATURE_LIVRAISON")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureLivraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature_livraison")
    private Long idCandidatureLivraison;

    @Column(name = "message_livreur", columnDefinition = "TEXT")
    private String messageLivreur;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCandidatureLivraison statut = StatutCandidatureLivraison.EN_ATTENTE;

    @Column(name = "date_candidature", nullable = false)
    private LocalDateTime dateCandidature;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "commentaire_client", columnDefinition = "TEXT")
    private String commentaireClient;

    @ManyToOne
    @JoinColumn(name = "id_annonce", nullable = false)
    private Annonce annonce;

    @ManyToOne
    @JoinColumn(name = "id_livreur", nullable = false)
    private Utilisateur livreur;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment_livraison")
    private SegmentLivraison segmentLivraison;

    @Column(name = "entrepot_choisi", length = 50)
    private String entrepotChoisi;

    @PrePersist
    protected void onCreate() {
        dateCandidature = LocalDateTime.now();
    }

    public enum SegmentLivraison {
        SEGMENT_1("Segment 1 - Départ vers entrepôt"),
        SEGMENT_2("Segment 2 - Entrepôt vers destination");

        private final String libelle;

        SegmentLivraison(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutCandidatureLivraison {
        EN_ATTENTE("En attente de réponse"),
        ACCEPTEE("Candidature acceptée"),
        REFUSEE("Candidature refusée");

        private final String libelle;

        StatutCandidatureLivraison(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean peutEtreAcceptee() {
        if (statut != StatutCandidatureLivraison.EN_ATTENTE || annonce == null) {
            return false;
        }

        if (segmentLivraison != null) {
            return annonce.getStatut() == Annonce.StatutAnnonce.PUBLIEE ||
                   annonce.getStatut() == Annonce.StatutAnnonce.SEGMENT_1_PRIS ||
                   annonce.getStatut() == Annonce.StatutAnnonce.SEGMENT_2_PRIS;
        }

        return annonce.getStatut() == Annonce.StatutAnnonce.PUBLIEE;
    }

    public boolean peutEtreRefusee() {
        return statut == StatutCandidatureLivraison.EN_ATTENTE;
    }

    public void accepter(String commentaire) {
        this.statut = StatutCandidatureLivraison.ACCEPTEE;
        this.dateReponse = LocalDateTime.now();
        this.commentaireClient = commentaire;
    }

    public void refuser(String commentaire) {
        this.statut = StatutCandidatureLivraison.REFUSEE;
        this.dateReponse = LocalDateTime.now();
        this.commentaireClient = commentaire;
    }
}
