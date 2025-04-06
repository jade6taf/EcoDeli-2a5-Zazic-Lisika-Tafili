package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "JUSTIFICATIF")
public class Justificatif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justificatif")
    private Integer idJustificatif;

    @Column(name = "type_justificatif", length = 100)
    private String typeJustificatif;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "validation_par_ad")
    private Boolean validationParAd = false;

    @Column(name = "chemin_fichier", length = 255)
    private String cheminFichier;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    public Justificatif() {
    }

    public Integer getIdJustificatif() {
        return idJustificatif;
    }

    public void setIdJustificatif(Integer idJustificatif) {
        this.idJustificatif = idJustificatif;
    }

    public String getTypeJustificatif() {
        return typeJustificatif;
    }

    public void setTypeJustificatif(String typeJustificatif) {
        this.typeJustificatif = typeJustificatif;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Boolean getValidationParAd() {
        return validationParAd;
    }

    public void setValidationParAd(Boolean validationParAd) {
        this.validationParAd = validationParAd;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}