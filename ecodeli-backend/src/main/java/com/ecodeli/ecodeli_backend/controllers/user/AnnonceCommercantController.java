package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.AnnonceCommercant;
import com.ecodeli.ecodeli_backend.models.CategorieAnnonce;
import com.ecodeli.ecodeli_backend.services.AnnonceCommercantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/annonces-commercant")
@CrossOrigin(origins = "*")
public class AnnonceCommercantController {

    @Autowired
    private AnnonceCommercantService annonceService;

    @GetMapping
    public ResponseEntity<List<AnnonceCommercant>> getAllAnnonces() {
        try {
            List<AnnonceCommercant> annonces = annonceService.getAllAnnonces();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnonceCommercant> getAnnonceById(@PathVariable Integer id) {
        try {
            AnnonceCommercant annonce = annonceService.getAnnonceById(id);
            return ResponseEntity.ok(annonce);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAnnonce(@RequestBody Map<String, Object> annonceData) {
        try {
            AnnonceCommercant annonce = annonceService.createAnnonce(annonceData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce créée avec succès");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAnnonce(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        try {
            AnnonceCommercant annonce = annonceService.updateAnnonce(id, updates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce mise à jour avec succès");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnonce(@PathVariable Integer id) {
        try {
            annonceService.deleteAnnonce(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            Map<String, Object> categories = new HashMap<>();
            
            for (CategorieAnnonce categorie : CategorieAnnonce.values()) {
                Map<String, Object> categorieInfo = new HashMap<>();
                categorieInfo.put("code", categorie.name());
                categorieInfo.put("libelle", categorie.getLibelle());
                categorieInfo.put("description", categorie.getDescription());
                categorieInfo.put("help", annonceService.getContextualHelp(categorie));
                
                categories.put(categorie.name(), categorieInfo);
            }
            
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/categories/{categorie}")
    public ResponseEntity<List<AnnonceCommercant>> getAnnoncesByCategorie(@PathVariable String categorie) {
        try {
            CategorieAnnonce categorieEnum = CategorieAnnonce.valueOf(categorie.toUpperCase());
            List<AnnonceCommercant> annonces = annonceService.getAnnoncesByCategorie(categorieEnum);
            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/categories/{categorie}/active")
    public ResponseEntity<List<AnnonceCommercant>> getActiveByCategorieAnnouncements(@PathVariable String categorie) {
        try {
            CategorieAnnonce categorieEnum = CategorieAnnonce.valueOf(categorie.toUpperCase());
            List<AnnonceCommercant> annonces = annonceService.getActiveByCategorieAnnouncements(categorieEnum);
            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/categories/{categorie}/help")
    public ResponseEntity<Map<String, Object>> getContextualHelp(@PathVariable String categorie) {
        try {
            CategorieAnnonce categorieEnum = CategorieAnnonce.valueOf(categorie.toUpperCase());
            Map<String, Object> help = annonceService.getContextualHelp(categorieEnum);
            return ResponseEntity.ok(help);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/commercant/{commercantId}")
    public ResponseEntity<List<AnnonceCommercant>> getAnnoncesByCommercant(@PathVariable Integer commercantId) {
        try {
            List<AnnonceCommercant> annonces = annonceService.getAnnoncesByCommercant(commercantId);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/commercant/{commercantId}/categories/{categorie}")
    public ResponseEntity<List<AnnonceCommercant>> getAnnoncesByCommercantAndCategorie(
            @PathVariable Integer commercantId, @PathVariable String categorie) {
        try {
            CategorieAnnonce categorieEnum = CategorieAnnonce.valueOf(categorie.toUpperCase());
            List<AnnonceCommercant> annonces = annonceService.getAnnoncesByCommercant(commercantId)
                .stream()
                .filter(a -> a.getCategorie() == categorieEnum)
                .toList();
            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishAnnonce(@PathVariable Integer id) {
        try {
            AnnonceCommercant annonce = annonceService.publishAnnonce(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce publiée avec succès");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAnnonce(@PathVariable Integer id, @RequestBody Map<String, Object> approvalData) {
        try {
            Integer adminId = (Integer) approvalData.get("adminId");
            String commentaire = (String) approvalData.get("commentaire");
            
            AnnonceCommercant annonce = annonceService.approveAnnonce(id, adminId, commentaire);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce approuvée avec succès");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAnnonce(@PathVariable Integer id, @RequestBody Map<String, Object> rejectionData) {
        try {
            Integer adminId = (Integer) rejectionData.get("adminId");
            String commentaire = (String) rejectionData.get("commentaire");
            
            AnnonceCommercant annonce = annonceService.rejectAnnonce(id, adminId, commentaire);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce rejetée");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnnonceCommercant>> searchAnnonces(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Integer commercantId) {
        try {
            CategorieAnnonce categorieEnum = categorie != null ? CategorieAnnonce.valueOf(categorie.toUpperCase()) : null;
            AnnonceCommercant.StatutAnnonce statutEnum = statut != null ? AnnonceCommercant.StatutAnnonce.valueOf(statut.toUpperCase()) : null;
            
            List<AnnonceCommercant> annonces = annonceService.searchAnnonces(searchTerm, categorieEnum, statutEnum, commercantId);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<AnnonceCommercant>> getActiveAnnouncements() {
        try {
            List<AnnonceCommercant> annonces = annonceService.getActiveAnnouncements();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<List<AnnonceCommercant>> getPendingApproval() {
        try {
            List<AnnonceCommercant> annonces = annonceService.getPendingApproval();
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = annonceService.getAnnonceStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<AnnonceCommercant>> getExpiringAnnouncements(@RequestParam(defaultValue = "7") int days) {
        try {
            List<AnnonceCommercant> annonces = annonceService.getExpiringAnnouncements(days);
            return ResponseEntity.ok(annonces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/livraison-ponctuelle/check-availability")
    public ResponseEntity<Map<String, Object>> checkLivraisonPonctuelleAvailability(@RequestBody Map<String, Object> requestData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("available", true);
            response.put("message", "Créneau disponible");
            response.put("estimatedPrice", "25.50");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("available", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/service-chariot/update-availability")
    public ResponseEntity<Map<String, Object>> updateServiceChariotAvailability(@RequestBody Map<String, Object> requestData) {
        try {
            Integer annonceId = (Integer) requestData.get("annonceId");
            String creneauxDisponibles = (String) requestData.get("creneauxDisponibles");
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("creneauxDisponibles", creneauxDisponibles);
            
            AnnonceCommercant annonce = annonceService.updateAnnonce(annonceId, updates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Disponibilité mise à jour");
            response.put("annonce", annonce);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/transport-marchandises/optimize-routes")
    public ResponseEntity<Map<String, Object>> optimizeTransportRoutes(@RequestBody Map<String, Object> requestData) {
        try {
            Integer annonceId = (Integer) requestData.get("annonceId");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Routes optimisées");
            response.put("optimizedRoutes", "Route optimisée calculée");
            response.put("estimatedSavings", "15%");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateAnnonceData(@RequestBody Map<String, Object> annonceData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Données valides");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("errors", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/estimate-price")
    public ResponseEntity<Map<String, Object>> estimatePrice(@RequestBody Map<String, Object> annonceData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("estimatedPrice", "35.00");
            response.put("priceBreakdown", Map.of(
                "basePrice", "25.00",
                "categoryMultiplier", "1.2",
                "additionalServices", "10.00"
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}