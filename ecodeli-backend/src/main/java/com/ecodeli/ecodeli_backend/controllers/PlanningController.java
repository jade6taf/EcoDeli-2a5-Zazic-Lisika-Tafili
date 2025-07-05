package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.services.PlanningService;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = "http://localhost:5173")
public class PlanningController {

    @Autowired
    private PlanningService planningService;

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/disponibilites")
    public ResponseEntity<?> getDisponibilites(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        try {
            Integer prestataireId = getCurrentPrestataireId();
            List<Map<String, Object>> disponibilites = planningService.getDisponibilitesForCalendar(prestataireId, start, end);
            return ResponseEntity.ok(disponibilites);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des disponibilités", e.getMessage()));
        }
    }

    @PostMapping("/disponibilites")
    public ResponseEntity<?> creerDisponibilite(@Valid @RequestBody DisponibiliteRequest request) {
        try {
            Integer prestataireId = getCurrentPrestataireId();

            if (Boolean.TRUE.equals(request.getRecurrent())) {
                List<DisponibilitePrestataire> creneaux = planningService.creerCreneauxRecurrents(prestataireId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(createSuccessResponse("Créneaux récurrents créés avec succès", 
                                Map.of("count", creneaux.size(), "creneaux", creneaux)));
            } else {
                DisponibilitePrestataire disponibilite = planningService.creerDisponibilite(prestataireId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(createSuccessResponse("Disponibilité créée avec succès", disponibilite));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Erreur de validation", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création de la disponibilité", e.getMessage()));
        }
    }

    @PutMapping("/disponibilites/{id}")
    public ResponseEntity<?> modifierDisponibilite(
            @PathVariable Long id,
            @Valid @RequestBody DisponibiliteRequest request) {

        try {
            Integer prestataireId = getCurrentPrestataireId();
            DisponibilitePrestataire disponibilite = planningService.modifierDisponibilite(prestataireId, id, request);
            return ResponseEntity.ok(createSuccessResponse("Disponibilité modifiée avec succès", disponibilite));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Erreur de validation", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès refusé", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la modification de la disponibilité", e.getMessage()));
        }
    }

    @DeleteMapping("/disponibilites/{id}")
    public ResponseEntity<?> supprimerDisponibilite(@PathVariable Long id) {
        try {
            Integer prestataireId = getCurrentPrestataireId();
            planningService.supprimerDisponibilite(prestataireId, id);
            return ResponseEntity.ok(createSuccessResponse("Disponibilité supprimée avec succès", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Accès refusé", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression de la disponibilité", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStatistiques() {
        try {
            Integer prestataireId = getCurrentPrestataireId();
            PlanningStatsResponse stats = planningService.getStatistiques(prestataireId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des statistiques", e.getMessage()));
        }
    }

    @GetMapping("/prochains-creneaux")
    public ResponseEntity<?> getProchainsCreneaux(
            @RequestParam(defaultValue = "5") int limite) {

        try {
            Integer prestataireId = getCurrentPrestataireId();
            List<DisponibilitePrestataire> creneaux = planningService.getProchainsCreneaux(prestataireId, limite);
            return ResponseEntity.ok(creneaux);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des prochains créneaux", e.getMessage()));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<?> getTypesDisponibilite() {
        try {
            Map<String, Object> types = new HashMap<>();
            for (TypeDisponibilite type : TypeDisponibilite.values()) {
                Map<String, String> typeInfo = new HashMap<>();
                typeInfo.put("libelle", type.getLibelle());
                typeInfo.put("couleur", type.getCouleur());
                types.put(type.name(), typeInfo);
            }
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des types", e.getMessage()));
        }
    }

    @GetMapping("/statuts")
    public ResponseEntity<?> getStatutsDisponibilite() {
        try {
            Map<String, String> statuts = new HashMap<>();
            for (StatutDisponibilite statut : StatutDisponibilite.values()) {
                statuts.put(statut.name(), statut.getLibelle());
            }
            return ResponseEntity.ok(statuts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la récupération des statuts", e.getMessage()));
        }
    }

    @PostMapping("/disponibilites/batch")
    public ResponseEntity<?> creerDisponibilitesBatch(@Valid @RequestBody List<DisponibiliteRequest> requests) {
        try {
            Integer prestataireId = getCurrentPrestataireId();
            List<DisponibilitePrestataire> disponibilites = new java.util.ArrayList<>();
            List<String> erreurs = new java.util.ArrayList<>();

            for (int i = 0; i < requests.size(); i++) {
                try {
                    DisponibilitePrestataire disponibilite = planningService.creerDisponibilite(prestataireId, requests.get(i));
                    disponibilites.add(disponibilite);
                } catch (Exception e) {
                    erreurs.add("Créneau " + (i + 1) + ": " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("created", disponibilites.size());
            result.put("errors", erreurs.size());
            result.put("disponibilites", disponibilites);
            if (!erreurs.isEmpty()) {
                result.put("errorDetails", erreurs);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Traitement terminé", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création en lot", e.getMessage()));
        }
    }

    private Integer getCurrentPrestataireId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return utilisateurService.getUtilisateurByEmail(email)
                .map(u -> {
                    if (u instanceof Prestataire) {
                        return u.getIdUtilisateur();
                    } else {
                        throw new RuntimeException("L'utilisateur connecté n'est pas un prestataire");
                    }
                })
                .orElseThrow(() -> {
                    return new RuntimeException("Utilisateur non trouvé avec l'email: " + email);
                });
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        if (details != null) {
            response.put("details", details);
        }
        return response;
    }
}
