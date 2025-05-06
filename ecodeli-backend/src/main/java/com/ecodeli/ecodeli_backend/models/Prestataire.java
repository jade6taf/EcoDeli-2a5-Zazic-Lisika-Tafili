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
    // Getters
    public String getNomEntreprise() { return nomEntreprise; }
    public String getSiret() { return siret; }
    public ServiceType getDomaineExpertise() { return domaineExpertise; }
    public String getZoneIntervention() { return zoneIntervention; }
    public Boolean getDisponible() { return disponible; }
    public Double getTarifHoraire() { return tarifHoraire; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Set<LocalDateTime> getDisponibilites() { return disponibilites; }
    public List<Evaluation> getEvaluations() { return evaluations; }

    // Setters
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    public void setSiret(String siret) { this.siret = siret; }
    public void setDomaineExpertise(ServiceType domaineExpertise) { this.domaineExpertise = domaineExpertise; }
    public void setZoneIntervention(String zoneIntervention) { this.zoneIntervention = zoneIntervention; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setDisponibilites(Set<LocalDateTime> disponibilites) { this.disponibilites = disponibilites; }
    public void setEvaluations(List<Evaluation> evaluations) { this.evaluations = evaluations; }

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
