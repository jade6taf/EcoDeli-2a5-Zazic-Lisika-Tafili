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
    @JoinColumn(name = "id_destinataire", nullable = false)
    private Utilisateur destinataire;

    @ManyToOne
    @JoinColumn(name = "id_colis")
    private Colis colis;

    public enum StatutLivraison { // De base une 'status' -> string, donc a modifier bdd
        EN_ATTENTE,
        EN_PREPARATION,
        EN_COURS,
        LIVRER,
        RETARDER,
        ANNULER,
        ECHEC
    }
}