package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @ElementCollection
    @CollectionTable(name = "disponibilites_prestataire",
        joinColumns = @JoinColumn(name = "id_prestataire"))
    private Set<LocalDateTime> disponibilites = new HashSet<>();

    @OneToMany(mappedBy = "prestataire")
    private List<Evaluation> evaluations = new ArrayList<>();
}
