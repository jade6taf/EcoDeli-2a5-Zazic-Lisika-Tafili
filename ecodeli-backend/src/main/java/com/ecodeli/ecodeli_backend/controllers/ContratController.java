package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Contrat;
import com.ecodeli.ecodeli_backend.models.ContratHistory;
import com.ecodeli.ecodeli_backend.services.ContratService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/commercants")
@CrossOrigin(origins = "http://localhost:5173")
public class ContratController {

    @Autowired
    private ContratService contratService;

    @GetMapping("/{idCommercant}/contrats")
    public ResponseEntity<List<Contrat>> getContratsCommercant(@PathVariable Integer idCommercant) {
        try {
            List<Contrat> contrats = contratService.getContratsCommercant(idCommercant);
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idCommercant}/contrats/{idContrat}")
    public ResponseEntity<?> getContratCommercant(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idContrat) {
        try {
            Optional<Contrat> contrat = contratService.getContratCommercant(idCommercant, idContrat);

            if (contrat.isPresent()) {
                return ResponseEntity.ok(contrat.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Contrat non trouvé ou non autorisé"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération du contrat"));
        }
    }

    @PostMapping("/{idCommercant}/contrats/{idContrat}/accept")
    public ResponseEntity<?> accepterContrat(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idContrat) {
        try {
            Contrat contratSigne = contratService.signerContratCommercant(idContrat, idCommercant);
            return ResponseEntity.ok(contratSigne);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la signature du contrat"));
        }
    }

    @PostMapping("/{idCommercant}/contrats/{idContrat}/refuse")
    public ResponseEntity<?> refuserContrat(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idContrat,
            @RequestBody Map<String, String> requestBody) {
        try {
            String motif = requestBody.getOrDefault("motif", "Aucun motif spécifié");
            Contrat contratRefuse = contratService.refuserContratCommercant(idContrat, idCommercant, motif);
            return ResponseEntity.ok(contratRefuse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors du refus du contrat"));
        }
    }

    @GetMapping("/{idCommercant}/contrats/{idContrat}/historique")
    public ResponseEntity<?> getHistoriqueContrat(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idContrat) {
        try {
            Optional<Contrat> contrat = contratService.getContratCommercant(idCommercant, idContrat);

            if (contrat.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Contrat non trouvé ou non autorisé"));
            }

            List<ContratHistory> historique = contratService.getHistoriqueContrat(idContrat);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération de l'historique"));
        }
    }

    @GetMapping("/{idCommercant}/contrats/statistiques")
    public ResponseEntity<?> getStatistiquesCommercant(@PathVariable Integer idCommercant) {
        try {
            Map<String, Object> statistiques = contratService.getStatistiquesCommercant(idCommercant);
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération des statistiques"));
        }
    }
}
