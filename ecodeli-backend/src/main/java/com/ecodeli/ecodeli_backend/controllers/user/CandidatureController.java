package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.services.CandidatureService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.PrestataireCategorie;
import com.ecodeli.ecodeli_backend.repositories.PrestataireCategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "*")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private PrestataireCategorieRepository prestataireCategorieRepository;

    /**
     * Récupérer toutes les candidatures pour une demande de service avec profils prestataires complets
     */
    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<Map<String, Object>> getCandidaturesByDemande(@PathVariable Long demandeId) {
        System.out.println("=== CONTROLLER: Récupération candidatures pour demande " + demandeId + " ===");
        
        try {
            List<Map<String, Object>> candidatures = candidatureService.getCandidaturesWithPrestatairesByDemande(demandeId);
            Map<String, Object> statistiques = candidatureService.getStatistiquesCandidaturesDemande(demandeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidatures", candidatures);
            response.put("statistiques", statistiques);
            response.put("message", candidatures.size() + " candidature(s) trouvée(s)");
            
            System.out.println("Candidatures récupérées avec succès: " + candidatures.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération candidatures: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Accepter une candidature (et refuser automatiquement les autres)
     */
    @PutMapping("/{candidatureId}/accepter")
    public ResponseEntity<Map<String, Object>> accepterCandidature(
            @PathVariable Long candidatureId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        System.out.println("=== CONTROLLER: Acceptation candidature " + candidatureId + " ===");
        
        try {
            String commentaireClient = "";
            if (requestBody != null && requestBody.containsKey("commentaire")) {
                commentaireClient = requestBody.get("commentaire");
            }
            
            Map<String, Object> result = candidatureService.accepterCandidature(candidatureId, commentaireClient);
            
            System.out.println("Candidature acceptée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR acceptation candidature: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Refuser une candidature
     */
    @PutMapping("/{candidatureId}/refuser")
    public ResponseEntity<Map<String, Object>> refuserCandidature(
            @PathVariable Long candidatureId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        System.out.println("=== CONTROLLER: Refus candidature " + candidatureId + " ===");
        
        try {
            String commentaireClient = "";
            if (requestBody != null && requestBody.containsKey("commentaire")) {
                commentaireClient = requestBody.get("commentaire");
            }
            
            Map<String, Object> result = candidatureService.refuserCandidature(candidatureId, commentaireClient);
            
            System.out.println("Candidature refusée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR refus candidature: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Obtenir les statistiques des candidatures pour une demande
     */
    @GetMapping("/demande/{demandeId}/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesCandidatures(@PathVariable Long demandeId) {
        try {
            Map<String, Object> statistiques = candidatureService.getStatistiquesCandidaturesDemande(demandeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistiques", statistiques);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Créer une candidature (endpoint pour prestataires)
     */
    @PostMapping("/creer")
    public ResponseEntity<Map<String, Object>> creerCandidature(@RequestBody Map<String, Object> candidatureData) {
        System.out.println("=== CONTROLLER: Création candidature prestataire ===");
        System.out.println("Données reçues: " + candidatureData);
        
        try {
            Long demandeId = Long.valueOf(candidatureData.get("demandeId").toString());
            String messagePrestataire = (String) candidatureData.get("messagePersonnalise");
            Integer delaiPropose = candidatureData.get("delaiPropose") != null ?
                Integer.valueOf(candidatureData.get("delaiPropose").toString()) : null;
            
            Integer prestataireId = null;
            if (candidatureData.get("prestataireId") != null) {
                prestataireId = Integer.valueOf(candidatureData.get("prestataireId").toString());
                System.out.println("ID prestataire reçu du frontend: " + prestataireId);
            } else {
                prestataireId = 1; // ID par défaut
            }
            
            System.out.println("DemandeId: " + demandeId);
            System.out.println("Message: " + messagePrestataire);
            System.out.println("Délai: " + delaiPropose);
            System.out.println("PrestataireId final: " + prestataireId);
            
            Map<String, Object> candidatureRequest = new HashMap<>();
            candidatureRequest.put("demandeId", demandeId);
            candidatureRequest.put("prestataireId", prestataireId);
            candidatureRequest.put("messagePrestataire", messagePrestataire);
            candidatureRequest.put("delaiPropose", delaiPropose);
            candidatureRequest.put("prixPropose", 25.0);
            
            Map<String, Object> result = candidatureService.creerCandidaturePrestataire(candidatureRequest);
            
            System.out.println("Candidature créée avec succès");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR création candidature: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByPrestataire(@PathVariable Integer prestataireId) {
        System.out.println("=== CONTROLLER: Récupération candidatures prestataire " + prestataireId + " ===");
        
        try {
            List<Map<String, Object>> candidatures = candidatureService.getCandidaturesByPrestataire(prestataireId);
            
            System.out.println("Candidatures trouvées: " + candidatures.size());
            return ResponseEntity.ok(candidatures);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération candidatures prestataire: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "CandidatureController fonctionne correctement");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}