package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Contrat;
import com.ecodeli.ecodeli_backend.models.ContratHistory;
import com.ecodeli.ecodeli_backend.models.StatutContrat;
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
@RequestMapping("/api/admin/contrats")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminContratController {

    @Autowired
    private ContratService contratService;

    @GetMapping
    public ResponseEntity<List<Contrat>> getAllContrats() {
        try {
            List<Contrat> contrats = contratService.getAllContrats();
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Contrat>> getContratsByStatut(@PathVariable StatutContrat statut) {
        try {
            List<Contrat> contrats = contratService.getContratsByStatut(statut);
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idContrat}")
    public ResponseEntity<?> getContratById(@PathVariable Integer idContrat) {
        try {
            Optional<Contrat> contrat = contratService.getContratById(idContrat);

            if (contrat.isPresent()) {
                return ResponseEntity.ok(contrat.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Contrat non trouvé"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération du contrat"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createContrat(
            @Valid @RequestBody Contrat contrat,
            @RequestParam Integer idAdmin) {
        try {
            Contrat nouveauContrat = contratService.createContrat(contrat, idAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouveauContrat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la création du contrat"));
        }
    }

    @PutMapping("/{idContrat}")
    public ResponseEntity<?> updateContrat(
            @PathVariable Integer idContrat,
            @Valid @RequestBody Contrat contratDetails,
            @RequestParam Integer idAdmin) {
        try {
            Contrat contratModifie = contratService.updateContrat(idContrat, contratDetails, idAdmin);
            return ResponseEntity.ok(contratModifie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la modification du contrat"));
        }
    }

    @PostMapping("/{idContrat}/send")
    public ResponseEntity<?> envoyerPourSignature(
            @PathVariable Integer idContrat,
            @RequestParam Integer idAdmin) {
        try {
            Contrat contrat = contratService.envoyerPourSignature(idContrat, idAdmin);
            return ResponseEntity.ok(contrat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de l'envoi du contrat"));
        }
    }

    @PostMapping("/{idContrat}/validate")
    public ResponseEntity<?> validerContrat(
            @PathVariable Integer idContrat,
            @RequestParam Integer idAdmin) {
        try {
            Contrat contrat = contratService.validerContratAdmin(idContrat, idAdmin);
            return ResponseEntity.ok(contrat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la validation du contrat"));
        }
    }

    @PostMapping("/{idContrat}/refuse")
    public ResponseEntity<?> refuserContrat(
            @PathVariable Integer idContrat,
            @RequestParam Integer idAdmin,
            @RequestBody Map<String, String> requestBody) {
        try {
            String motif = requestBody.getOrDefault("motif", "Aucun motif spécifié");
            Contrat contrat = contratService.refuserContratAdmin(idContrat, idAdmin, motif);
            return ResponseEntity.ok(contrat);
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

    @DeleteMapping("/{idContrat}")
    public ResponseEntity<?> deleteContrat(
            @PathVariable Integer idContrat,
            @RequestParam Integer idAdmin) {
        try {
            contratService.deleteContrat(idContrat, idAdmin);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la suppression du contrat"));
        }
    }

    @GetMapping("/{idContrat}/historique")
    public ResponseEntity<?> getHistoriqueContrat(@PathVariable Integer idContrat) {
        try {
            Optional<Contrat> contrat = contratService.getContratById(idContrat);

            if (contrat.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Contrat non trouvé"));
            }

            List<ContratHistory> historique = contratService.getHistoriqueContrat(idContrat);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération de l'historique"));
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiquesAdmin() {
        try {
            Map<String, Object> statistiques = contratService.getStatistiquesAdmin();
            return ResponseEntity.ok(statistiques);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération des statistiques"));
        }
    }
}
