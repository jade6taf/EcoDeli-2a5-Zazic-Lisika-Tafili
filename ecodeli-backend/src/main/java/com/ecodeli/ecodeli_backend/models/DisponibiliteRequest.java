package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteRequest {
    private Long id;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @NotNull(message = "Le type de disponibilité est obligatoire")
    private TypeDisponibilite type;

    private StatutDisponibilite statut = StatutDisponibilite.ACTIVE;

    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    private Boolean recurrent = false;

    private List<String> joursRecurrence; // ["LUNDI", "MERCREDI"]

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finRecurrence;

    public boolean isValid() {
        if (dateDebut == null || dateFin == null) {
            return false;
        }

        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            return false;
        }

        if (Boolean.TRUE.equals(recurrent) && (joursRecurrence == null || joursRecurrence.isEmpty())) {
            return false;
        }

        return true;
    }

    public String getValidationError() {
        if (dateDebut == null || dateFin == null) {
            return "Les dates de début et fin sont obligatoires";
        }

        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            return "La date de fin doit être postérieure à la date de début";
        }

        if (Boolean.TRUE.equals(recurrent) && (joursRecurrence == null || joursRecurrence.isEmpty())) {
            return "Veuillez sélectionner au moins un jour pour la récurrence";
        }
        return null;
    }
}
