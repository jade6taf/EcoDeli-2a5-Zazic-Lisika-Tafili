package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@Entity
@Table(name = "UTILISATEUR")
@Inheritance(strategy = InheritanceType.JOINED)
// La colonne type_utilisateur dans la base de données stocke le type d'utilisateur (CLIENT, LIVREUR, etc.)
@DiscriminatorColumn(name = "type_utilisateur", discriminatorType = DiscriminatorType.STRING)
// Le champ "type" dans le JSON est utilisé pour la désérialisation et correspond à la colonne type_utilisateur
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Client.class, name = "CLIENT"),
    @JsonSubTypes.Type(value = Livreur.class, name = "LIVREUR"),
    @JsonSubTypes.Type(value = Commercant.class, name = "COMMERCANT"),
    @JsonSubTypes.Type(value = Prestataire.class, name = "PRESTATAIRE")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    @Column(name = "prenom", length = 100, nullable = false)
    private String prenom;

    @Column(name = "genre")
    private Boolean genre;

    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "date_de_naissance")
    private LocalDate dateDeNaissance;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 255, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Column(name = "mot_de_passe", length = 255, nullable = false)
    private String motDePasse;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,10}$", message = "Le numéro de téléphone n'est pas valide")
    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    @Column(name = "ville", length = 100)
    private String ville;

    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    @Column(name = "code_postal", length = 10)
    private String codePostal;

    @Size(max = 50, message = "Le pays ne peut pas dépasser 50 caractères")
    @Column(name = "pays", length = 50)
    private String pays;

    @JsonIgnore
    public String getType() {
        if (this instanceof Client)
            return "CLIENT";
        if (this instanceof Livreur)
            return "LIVREUR";
        if (this instanceof Commercant)
            return "COMMERCANT";
        if (this instanceof Prestataire)
            return "PRESTATAIRE";
        return null;
    }

}
