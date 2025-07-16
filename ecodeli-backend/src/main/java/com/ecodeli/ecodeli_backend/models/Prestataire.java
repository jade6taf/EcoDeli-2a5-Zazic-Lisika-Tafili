package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("PRESTATAIRE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Prestataire extends Utilisateur {
    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Column(name = "nom_entreprise", nullable = false)
    private String nomEntreprise;

    @Column(name = "siret", length = 14)
    @Pattern(regexp = "^[0-9]{14}$", message = "Le numéro SIRET doit contenir 14 chiffres")
    private String siret;

    @Enumerated(EnumType.STRING)
    @Column(name = "domaine_expertise")
    private ServiceType domaineExpertise;

    @Column(name = "zone_intervention", columnDefinition = "TEXT")
    private String zoneIntervention;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "tarif_horaire")
    private Double tarifHoraire;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation")
    private StatutValidationPrestataire statutValidation = StatutValidationPrestataire.EN_ATTENTE;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "commentaire_validation", columnDefinition = "TEXT")
    private String commentaireValidation;

    @ManyToOne
    @JoinColumn(name = "id_admin_validateur")
    private Admin adminValidateur;

    @ElementCollection
    @CollectionTable(name = "disponibilites_prestataire",
        joinColumns = @JoinColumn(name = "id_prestataire"))
    private Set<LocalDateTime> disponibilites = new HashSet<>();

    public enum StatutValidationPrestataire {
        EN_ATTENTE("En attente de validation"),
        VALIDE("Prestataire validé"),
        REJETE("Prestataire rejeté"),
        SUSPENDU("Prestataire suspendu");

        private final String libelle;

        StatutValidationPrestataire(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public boolean estValide() {
        return statutValidation == StatutValidationPrestataire.VALIDE;
    }

    public boolean estEnAttente() {
        return statutValidation == StatutValidationPrestataire.EN_ATTENTE;
    }

    public boolean estRejete() {
        return statutValidation == StatutValidationPrestataire.REJETE;
    }

    public void valider(Admin admin, String commentaire) {
        this.statutValidation = StatutValidationPrestataire.VALIDE;
        this.adminValidateur = admin;
        this.commentaireValidation = commentaire;
        this.dateValidation = LocalDateTime.now();
    }

    public void rejeter(Admin admin, String commentaire) {
        this.statutValidation = StatutValidationPrestataire.REJETE;
        this.adminValidateur = admin;
        this.commentaireValidation = commentaire;
        this.dateValidation = LocalDateTime.now();
    }

    public void suspendre(Admin admin, String commentaire) {
        this.statutValidation = StatutValidationPrestataire.SUSPENDU;
        this.adminValidateur = admin;
        this.commentaireValidation = commentaire;
        this.dateValidation = LocalDateTime.now();
    }
}
