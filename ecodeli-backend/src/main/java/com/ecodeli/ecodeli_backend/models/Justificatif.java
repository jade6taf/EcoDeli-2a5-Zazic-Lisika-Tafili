package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "JUSTIFICATIF")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @NotBlank(message = "Le chemin du fichier est obligatoire")
    @Column(name = "chemin_fichier", length = 255)
    private String cheminFichier;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;
}