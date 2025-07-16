package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.services.PaiementFictifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiement")
@CrossOrigin(origins = "http://localhost:5173")
public class PaiementController {

    @Autowired
    private PaiementFictifService paiementService;

    /**
     * Effectue le paiement d'une mission (simulation)
     */
    @PostMapping("/mission/{candidatureId}")
    public ResponseEntity<Map<String, Object>> payerMission(
            @PathVariable Long candidatureId,
            @RequestBody Map<String, Object> paiementData) {
        
        System.out.println("=== CONTROLLER: Paiement mission " + candidatureId + " ===");
        System.out.println("Données paiement: " + paiementData);
        
        try {
            BigDecimal montantTotal = new BigDecimal(paiementData.get("montant").toString());
            
            Map<String, Object> result = paiementService.simulerPaiementMission(candidatureId, montantTotal);
            
            if ((Boolean) result.get("success")) {
                System.out.println("Paiement effectué avec succès");
                return ResponseEntity.ok(result);
            } else {
                System.err.println("Erreur paiement: " + result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR paiement mission: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupère le portefeuille d'un prestataire
     */
    @GetMapping("/portefeuille/prestataire/{prestataireId}")
    public ResponseEntity<Map<String, Object>> getPortefeuillePrestataire(@PathVariable Integer prestataireId) {
        System.out.println("=== CONTROLLER: Récupération portefeuille prestataire " + prestataireId + " ===");
        
        try {
            Map<String, Object> portefeuille = paiementService.getPortefeuillePrestataire(prestataireId);
            
            return ResponseEntity.ok(portefeuille);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération portefeuille: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Effectue un retrait pour un prestataire
     */
    @PostMapping("/portefeuille/prestataire/{prestataireId}/retrait")
    public ResponseEntity<Map<String, Object>> effectuerRetrait(
            @PathVariable Integer prestataireId,
            @RequestBody Map<String, Object> retraitData) {
        
        System.out.println("=== CONTROLLER: Retrait prestataire " + prestataireId + " ===");
        System.out.println("Données retrait: " + retraitData);
        
        try {
            BigDecimal montantRetrait = new BigDecimal(retraitData.get("montant").toString());
            String iban = (String) retraitData.get("iban");
            
            Map<String, Object> result = paiementService.simulerRetraitPrestataire(prestataireId, montantRetrait, iban);
            
            if ((Boolean) result.get("success")) {
                System.out.println("Retrait effectué avec succès");
                return ResponseEntity.ok(result);
            } else {
                System.err.println("Erreur retrait: " + result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR retrait: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupère l'historique des transactions d'un prestataire
     */
    @GetMapping("/portefeuille/prestataire/{prestataireId}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getHistoriqueTransactions(
            @PathVariable Integer prestataireId,
            @RequestParam(defaultValue = "20") int limit) {
        
        System.out.println("=== CONTROLLER: Historique transactions prestataire " + prestataireId + " ===");
        
        try {
            List<Map<String, Object>> transactions = paiementService.getHistoriqueTransactions(prestataireId, limit);
            
            System.out.println("Transactions trouvées: " + transactions.size());
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour les informations bancaires d'un prestataire
     */
    @PutMapping("/portefeuille/prestataire/{prestataireId}/infos-bancaires")
    public ResponseEntity<Map<String, Object>> updateInfosBancaires(
            @PathVariable Integer prestataireId,
            @RequestBody Map<String, Object> infosData) {
        
        System.out.println("=== CONTROLLER: Mise à jour infos bancaires prestataire " + prestataireId + " ===");
        
        try {
            String iban = (String) infosData.get("iban");
            String nomTitulaire = (String) infosData.get("nomTitulaire");
            
            Map<String, Object> result = paiementService.updateInfosBancaires(prestataireId, iban, nomTitulaire);
            
            System.out.println("Infos bancaires mises à jour");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR mise à jour infos bancaires: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verifier-carte")
    public ResponseEntity<Map<String, Object>> verifierCarte(@RequestBody Map<String, Object> carteData) {
        System.out.println("=== CONTROLLER: Vérification carte bancaire (fictif) ===");
        
        try {
            Thread.sleep(1000);
            
            String numeroMasque = (String) carteData.get("numeroMasque");
            String nomTitulaire = (String) carteData.get("nomTitulaire");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Carte vérifiée avec succès");
            result.put("numeroMasque", numeroMasque != null ? numeroMasque : "**** **** **** 1234");
            result.put("nomTitulaire", nomTitulaire != null ? nomTitulaire : "Client EcoDeli");
            result.put("typecarte", "Visa");
            result.put("banque", "Banque Fictive");
            
            return ResponseEntity.ok(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Interruption lors de la vérification");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("ERREUR vérification carte: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupère le total dépensé par un client (livraisons + services)
     */
    @GetMapping("/client/{clientId}/total-depense")
    public ResponseEntity<Map<String, Object>> getTotalDepenseClient(@PathVariable Integer clientId) {
        System.out.println("=== CONTROLLER: Total dépensé client " + clientId + " ===");
        
        try {
            Map<String, Object> result = paiementService.getTotalDepenseClient(clientId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERREUR calcul total dépensé: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupère les statistiques de paiement (pour admin/monitoring)
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesPaiement() {
        System.out.println("=== CONTROLLER: Statistiques paiement ===");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTransactions", 150);
            stats.put("montantTotalTradé", new BigDecimal("5000.0"));
            stats.put("commissionsEcodeli", new BigDecimal("200.0"));
            stats.put("nombrePrestatairesActifs", 25);
            stats.put("transactionsAujourdHui", 8);
            stats.put("montantMoyenTransaction", new BigDecimal("35.0"));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("ERREUR récupération statistiques: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}