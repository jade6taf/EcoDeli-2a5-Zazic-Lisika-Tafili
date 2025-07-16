package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRESTATAIRE_CATEGORIE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestataireCategorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestataire_categorie")
    private Long idPrestataireCategorie;

    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Prestataire prestataire;

    @NotNull(message = "La catégorie de service est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_service", nullable = false)
    private ServiceType categorieService;

    @NotNull(message = "Le statut de validation est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(name = "tarif_horaire")
    private Double tarifHoraire;

    @Column(name = "commentaire_admin", columnDefinition = "TEXT")
    private String commentaireAdmin;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @ManyToOne
    @JoinColumn(name = "id_admin_validateur")
    private Admin adminValidateur;

    @PrePersist
    protected void onCreate() {
        dateDemande = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (statutValidation != StatutValidation.EN_ATTENTE) {
            dateValidation = LocalDateTime.now();
        }
    }

    public enum StatutValidation {
        EN_ATTENTE("En attente de validation"),
        VALIDE("Validé par l'administrateur"),
        REJETE("Rejeté par l'administrateur");

        private final String libelle;

        StatutValidation(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean estValide() {
        return statutValidation == StatutValidation.VALIDE;
    }

    public boolean estEnAttente() {
        return statutValidation == StatutValidation.EN_ATTENTE;
    }

    public boolean estRejete() {
        return statutValidation == StatutValidation.REJETE;
    }

    public boolean peutEtreModifie() {
        return statutValidation == StatutValidation.EN_ATTENTE || statutValidation == StatutValidation.REJETE;
    }

    public void valider(Admin admin, String commentaire) {
        this.statutValidation = StatutValidation.VALIDE;
        this.adminValidateur = admin;
        this.commentaireAdmin = commentaire;
        this.dateValidation = LocalDateTime.now();
    }

    public void rejeter(Admin admin, String commentaire) {
        this.statutValidation = StatutValidation.REJETE;
        this.adminValidateur = admin;
        this.commentaireAdmin = commentaire;
        this.dateValidation = LocalDateTime.now();
    }
}