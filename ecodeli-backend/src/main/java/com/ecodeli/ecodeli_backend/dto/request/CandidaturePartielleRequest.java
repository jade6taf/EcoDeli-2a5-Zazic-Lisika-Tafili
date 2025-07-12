package com.ecodeli.ecodeli_backend.dto.request;

import com.ecodeli.ecodeli_backend.models.CandidatureLivraison.SegmentLivraison;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidaturePartielleRequest {

    @NotNull(message = "L'ID de l'annonce est obligatoire")
    private Integer annonceId;

    @NotNull(message = "L'ID du livreur est obligatoire")
    private Integer livreurId;

    @NotNull(message = "Le segment est obligatoire")
    private SegmentLivraison segment;

    private String entrepotChoisi;

    private String messageLivreur;

    public boolean isValid() {
        if (segment == SegmentLivraison.SEGMENT_1) {
            return entrepotChoisi != null && !entrepotChoisi.trim().isEmpty();
        }
        return true;
    }
}
