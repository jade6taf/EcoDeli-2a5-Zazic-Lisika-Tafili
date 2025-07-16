package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.services.DemandeServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes-service")
@CrossOrigin(origins = "*")
public class DemandeServiceController {

    @Autowired
    private DemandeServiceService demandeServiceService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> creerDemandeService(@RequestBody Map<String, Object> demandeData) {
        try {
            DemandeService demande = demandeServiceService.creerDemandeService(demandeData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", demande);
            response.put("message", "Demande créée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERREUR lors de la création de demande: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupérer toutes les demandes de service
     */
    @GetMapping
    public ResponseEntity<List<DemandeService>> getAllDemandesService() {
        List<DemandeService> demandes = demandeServiceService.getAllDemandesService();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupérer une demande de service par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemandeService> getDemandeServiceById(@PathVariable Long id) {
        try {
            DemandeService demande = demandeServiceService.getDemandeServiceById(id);
            return ResponseEntity.ok(demande);
        } catch (Exception e) {
            System.err.println("ERREUR récupération demande: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer les demandes de service d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<DemandeService>> getDemandesServiceByClient(@PathVariable Integer clientId) {
        List<DemandeService> demandes = demandeServiceService.getDemandesServiceByClient(clientId);
        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupérer les demandes de service par catégorie
     */
    @GetMapping("/categorie/{categorieService}")
    public ResponseEntity<List<DemandeService>> getDemandesServiceByCategorie(@PathVariable String categorieService) {
        List<DemandeService> demandes = demandeServiceService.getDemandesServiceByCategorie(categorieService);
        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupérer les demandes de service disponibles (publiées)
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<DemandeService>> getDemandesServiceDisponibles() {
        List<DemandeService> demandes = demandeServiceService.getDemandesServiceDisponibles();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Mettre à jour une demande de service
     */
    @PutMapping("/{id}")
    public ResponseEntity<DemandeService> mettreAJourDemandeService(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> demandeData) {
        try {
            DemandeService demande = demandeServiceService.mettreAJourDemandeService(id, demandeData);
            return ResponseEntity.ok(demande);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Changer le statut d'une demande de service
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, Object>> changerStatutDemandeService(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statutData) {
        try {
            String nouveauStatut = statutData.get("statut");
            demandeServiceService.changerStatutDemandeService(id, nouveauStatut);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Statut mis à jour avec succès");
            response.put("nouveauStatut", nouveauStatut);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Annuler une demande de service
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> annulerDemandeService(@PathVariable Long id) {
        try {
            demandeServiceService.annulerDemandeService(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande de service annulée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Rechercher des demandes de service avec filtres
     */
    @PostMapping("/recherche")
    public ResponseEntity<List<DemandeService>> rechercherDemandesService(@RequestBody Map<String, Object> filtres) {
        List<DemandeService> demandes = demandeServiceService.rechercherDemandesService(filtres);
        return ResponseEntity.ok(demandes);
    }

    /**
     * Obtenir les statistiques des demandes de service
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesDemandesService() {
        Map<String, Object> stats = demandeServiceService.getStatistiquesDemandesService();
        return ResponseEntity.ok(stats);
    }
}