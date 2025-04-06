package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LIVRAISON")
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livraison")
    private Integer idLivraison;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "statut", length = 50)
    private String statut;

    @Column(name = "adresse_de_livraison", length = 100)
    private String adresseDeLivraison;

    @Column(name = "code_postal_livraison", length = 10)
    private String codePostalLivraison;

    @Column(name = "adresse_envoi", length = 100)
    private String adresseEnvoi;

    @Column(name = "code_postal_envoi", length = 10)
    private String codePostalEnvoi;

    @Column(name = "prix")
    private Integer prix;

    @Column(name = "code_validation", length = 10)
    private String codeValidation;

    @Column(name = "validation")
    private Boolean validation = false;

    @ManyToOne
    @JoinColumn(name = "id_expediteur", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne
    @JoinColumn(name = "id_destinataire", nullable = false)
    private Utilisateur destinataire;

    @ManyToOne
    @JoinColumn(name = "id_colis")
    private Colis colis;

    public Livraison() {
    }

    public Integer getIdLivraison() {
        return idLivraison;
    }

    public void setIdLivraison(Integer idLivraison) {
        this.idLivraison = idLivraison;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAdresseDeLivraison() {
        return adresseDeLivraison;
    }

    public void setAdresseDeLivraison(String adresseDeLivraison) {
        this.adresseDeLivraison = adresseDeLivraison;
    }

    public String getCodePostalLivraison() {
        return codePostalLivraison;
    }

    public void setCodePostalLivraison(String codePostalLivraison) {
        this.codePostalLivraison = codePostalLivraison;
    }

    public String getAdresseEnvoi() {
        return adresseEnvoi;
    }

    public void setAdresseEnvoi(String adresseEnvoi) {
        this.adresseEnvoi = adresseEnvoi;
    }

    public String getCodePostalEnvoi() {
        return codePostalEnvoi;
    }

    public void setCodePostalEnvoi(String codePostalEnvoi) {
        this.codePostalEnvoi = codePostalEnvoi;
    }

    public Integer getPrix() {
        return prix;
    }

    public void setPrix(Integer prix) {
        this.prix = prix;
    }

    public String getCodeValidation() {
        return codeValidation;
    }

    public void setCodeValidation(String codeValidation) {
        this.codeValidation = codeValidation;
    }

    public Boolean getValidation() {
        return validation;
    }

    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    public Utilisateur getExpediteur() {
        return expediteur;
    }

    public void setExpediteur(Utilisateur expediteur) {
        this.expediteur = expediteur;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(Utilisateur destinataire) {
        this.destinataire = destinataire;
    }

    public Colis getColis() {
        return colis;
    }

    public void setColis(Colis colis) {
        this.colis = colis;
    }
}