package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "disponibilites_prestataire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilitePrestataire {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestataire", nullable = false)
    @JsonIgnore
    private Prestataire prestataire;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull(message = "Le type de disponibilité est obligatoire")
    private TypeDisponibilite type = TypeDisponibilite.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @NotNull(message = "Le statut est obligatoire")
    private StatutDisponibilite statut = StatutDisponibilite.ACTIVE;

    @Column(name = "titre", length = 100)
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Column(name = "recurrent")
    private Boolean recurrent = false;

    @Column(name = "jours_recurrence", columnDefinition = "TEXT")
    private String joursRecurrence; // JSON: ["LUNDI", "MERCREDI"]

    @Column(name = "fin_recurrence")
    private LocalDate finRecurrence;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return StatutDisponibilite.ACTIVE.equals(this.statut);
    }

    public boolean isConflictWith(LocalDateTime debut, LocalDateTime fin) {
        return !(this.dateFin.isBefore(debut) || this.dateDebut.isAfter(fin));
    }

    public String getCouleur() {
        return this.type != null ? this.type.getCouleur() : "#6b7280";
    }

    public String getLibelleType() {
        return this.type != null ? this.type.getLibelle() : "Non défini";
    }
}
