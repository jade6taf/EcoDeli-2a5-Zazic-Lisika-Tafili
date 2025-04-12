package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ENTREPRISE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entreprise")
    private Integer idEntreprise;

    @Pattern(regexp = "^[0-9]{14}$", message = "Le SIRET doit contenir 14 chiffres")
    @Column(name = "SIRET", length = 50)
    private String siret;

    @Column(name = "statut_juridique")
    private Integer statutJuridique;

    @Column(name = "secteur_activite", length = 100)
    private String secteurActivite;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,10}$", message = "Le numéro de téléphone n'est pas valide")
    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "code_postal", length = 10)
    private String codePostal;

    @Column(name = "pays", length = 50)
    private String pays;

    @Column(name = "site_web", length = 50)
    private String siteWeb;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout;

    @Column(name = "validation_par_ad")
    private Boolean validationParAd = false;
}