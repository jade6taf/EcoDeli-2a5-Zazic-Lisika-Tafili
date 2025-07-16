package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.services.MissionService;
import com.ecodeli.ecodeli_backend.services.EvaluationService;
import com.ecodeli.ecodeli_backend.services.PrixCalculService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:5173")
public class MissionController {

    @Autowired
    private MissionService missionService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private PrixCalculService prixCalculService;

    /**
     * Récupérer les missions d'un prestataire
     */
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<Map<String, Object>>> getMissionsByPrestataire(@PathVariable Integer prestataireId) {
        System.out.println("=== CONTROLLER: Récupération missions prestataire " + prestataireId + " ===");
        
        try {
            List<Map<String, Object>> missions = missionService.getMissionsByPrestataire(prestataireId);
            
            System.out.println("Missions trouvées: " + missions.size());
            return ResponseEntity.ok(missions);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération missions prestataire: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Démarrer une mission
     */
    @PostMapping("/{missionId}/start")
    public ResponseEntity<Map<String, Object>> demarrerMission(@PathVariable Long missionId) {
        System.out.println("=== CONTROLLER: Démarrage mission " + missionId + " ===");
        
        try {
            Map<String, Object> result = missionService.demarrerMission(missionId);
            
            System.out.println("Mission démarrée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR démarrage mission: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Terminer une mission
     */
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<Map<String, Object>> terminerMission(
            @PathVariable Long missionId,
            @RequestBody Map<String, Object> completionData) {
        
        System.out.println("=== CONTROLLER: Terminaison mission " + missionId + " ===");
        System.out.println("Données reçues: " + completionData);
        
        try {
            String noteFinale = (String) completionData.get("noteFinale");
            String photoFinale = (String) completionData.get("photoFinale");
            
            Map<String, Object> result = missionService.terminerMission(missionId, noteFinale, photoFinale);
            
            System.out.println("Mission terminée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR terminaison mission: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupérer les détails d'une mission
     */
    @GetMapping("/{missionId}")
    public ResponseEntity<Map<String, Object>> getDetailsMission(@PathVariable Long missionId) {
        System.out.println("=== CONTROLLER: Récupération détails mission " + missionId + " ===");
        
        try {
            Map<String, Object> mission = missionService.getDetailsMission(missionId);
            
            if (mission != null) {
                return ResponseEntity.ok(mission);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Mission non trouvée");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération détails mission: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer les missions d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Map<String, Object>>> getMissionsByClient(@PathVariable Integer clientId) {
        System.out.println("=== CONTROLLER: Récupération missions client " + clientId + " ===");
        
        try {
            List<Map<String, Object>> missions = missionService.getMissionsByClient(clientId);
            
            System.out.println("Missions trouvées: " + missions.size());
            return ResponseEntity.ok(missions);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération missions client: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Valider une mission terminée (côté client)
     */
    @PostMapping("/{missionId}/validate")
    public ResponseEntity<Map<String, Object>> validerMission(
            @PathVariable Long missionId,
            @RequestBody Map<String, Object> validationData) {
        
        System.out.println("=== CONTROLLER: Validation mission " + missionId + " ===");
        System.out.println("Données validation: " + validationData);
        
        try {
            Map<String, Object> result = missionService.validerMission(missionId, validationData);
            
            System.out.println("Mission validée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR validation mission: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Créer une évaluation pour une mission
     */
    @PostMapping("/{missionId}/evaluation")
    public ResponseEntity<Map<String, Object>> creerEvaluation(
            @PathVariable Long missionId,
            @RequestBody Map<String, Object> evaluationData) {
        
        System.out.println("=== CONTROLLER: Création évaluation mission " + missionId + " ===");
        System.out.println("Données évaluation: " + evaluationData);
        
        try {
            Map<String, Object> result = evaluationService.creerEvaluation(missionId, evaluationData);
            
            System.out.println("Évaluation créée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR création évaluation: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupérer les statistiques d'évaluations d'un prestataire
     */
    @GetMapping("/prestataire/{prestataireId}/evaluations/stats")
    public ResponseEntity<Map<String, Object>> getStatistiquesEvaluationsPrestataire(@PathVariable Integer prestataireId) {
        System.out.println("=== CONTROLLER: Statistiques évaluations prestataire " + prestataireId + " ===");
        
        try {
            Map<String, Object> stats = evaluationService.getStatistiquesPrestataire(prestataireId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération stats évaluations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer toutes les évaluations d'un prestataire
     */
    @GetMapping("/prestataire/{prestataireId}/evaluations")
    public ResponseEntity<List<Map<String, Object>>> getEvaluationsPrestataire(@PathVariable Integer prestataireId) {
        System.out.println("=== CONTROLLER: Toutes évaluations prestataire " + prestataireId + " ===");
        
        try {
            List<Map<String, Object>> evaluations = evaluationService.getEvaluationsPrestataire(prestataireId);
            
            System.out.println("Évaluations trouvées: " + evaluations.size());
            return ResponseEntity.ok(evaluations);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération évaluations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer les dernières évaluations avec commentaires d'un prestataire
     */
    @GetMapping("/prestataire/{prestataireId}/evaluations/commentaires")
    public ResponseEntity<List<Map<String, Object>>> getDernieresEvaluationsAvecCommentaires(
            @PathVariable Integer prestataireId,
            @RequestParam(defaultValue = "5") int limit) {
        
        System.out.println("=== CONTROLLER: Dernières évaluations avec commentaires prestataire " + prestataireId + " ===");
        
        try {
            List<Map<String, Object>> evaluations = evaluationService
                .getDernieresEvaluationsAvecCommentaires(prestataireId, limit);
            
            System.out.println("Évaluations avec commentaires trouvées: " + evaluations.size());
            return ResponseEntity.ok(evaluations);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération évaluations commentaires: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer l'évaluation d'une mission spécifique
     */
    @GetMapping("/{missionId}/evaluation")
    public ResponseEntity<Map<String, Object>> getEvaluationMission(@PathVariable Long missionId) {
        System.out.println("=== CONTROLLER: Récupération évaluation mission " + missionId + " ===");
        
        try {
            Map<String, Object> evaluation = evaluationService.getEvaluationByCandidature(missionId);
            
            if (evaluation != null) {
                return ResponseEntity.ok(evaluation);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Aucune évaluation trouvée pour cette mission");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération évaluation mission: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère le prix calculé d'une mission basé sur le tarif réel du prestataire
     */
    @GetMapping("/{missionId}/prix-calcule")
    public ResponseEntity<Map<String, Object>> getPrixCalculeMission(@PathVariable Long missionId) {
        System.out.println("=== CONTROLLER: Prix calculé mission " + missionId + " ===");
        
        try {
            Map<String, Object> details = prixCalculService.getDetailsPrix(missionId);
            
            if (details.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Mission non trouvée ou prix incalculable");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            System.out.println("Prix calculé: " + details.get("prixTotal") + "€");
            return ResponseEntity.ok(details);
            
        } catch (Exception e) {
            System.err.println("ERREUR prix calculé: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}