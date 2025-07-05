package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationDetailsDTO {
    @NotBlank(message = "Le type de formation est obligatoire")
    @Pattern(regexp = "^(cours_particuliers|soutien_scolaire|formation_pro|coaching|langue)$",
             message = "Type de formation invalide")
    private String typeFormation;

    @NotBlank(message = "La matière/domaine est obligatoire")
    @Size(min = 2, max = 100, message = "La matière doit faire entre 2 et 100 caractères")
    private String matiereDomaine;

    @NotBlank(message = "Le niveau est obligatoire")
    @Pattern(regexp = "^(debutant|intermediaire|avance|expert)$",
             message = "Niveau invalide")
    private String niveau;

    @NotBlank(message = "La modalité est obligatoire")
    @Pattern(regexp = "^(presentiel|en_ligne)$",
             message = "Modalité invalide")
    private String modalite;

    private String objectifs;
    private String frequenceSouhaitee;
    private String lieuFormation;
    private String niveauActuel;

    private Boolean preparationExamen;
    private String nomExamen;
    private String dateExamen;

    private String methodePedagogique;
    private String supportsCours;
    private Boolean devoirs;

    @Min(value = 1, message = "La durée par session doit être au moins 1h")
    @Max(value = 8, message = "La durée par session ne peut pas dépasser 8h")
    private Integer dureeParSession;

    @Min(value = 1, message = "Le nombre de sessions doit être au moins 1")
    @Max(value = 50, message = "Le nombre de sessions ne peut pas dépasser 50")
    private Integer nombreSessions;

    private String ageApprenant;
    private String handicapAdaptation;
    private String materielRequis;
    private String disponibilites;
}
