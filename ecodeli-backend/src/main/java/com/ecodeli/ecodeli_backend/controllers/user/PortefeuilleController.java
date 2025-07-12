package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Retrait;
import com.ecodeli.ecodeli_backend.services.PortefeuilleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portefeuille")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PortefeuilleController {

    private final PortefeuilleService portefeuilleService;

    @GetMapping("/livreur/{livreurId}")
    public ResponseEntity<?> getPortefeuille(@PathVariable Integer livreurId) {
        try {
            Livreur livreur = portefeuilleService.getPortefeuille(livreurId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("soldePortefeuille", livreur.getSoldePortefeuille());
            response.put("totalGagnes", livreur.getTotalGagnes());
            response.put("ibanLivreur", livreur.getIbanLivreur());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération du portefeuille: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/retrait/{livreurId}")
    public ResponseEntity<?> demanderRetrait(
            @PathVariable Integer livreurId,
            @RequestBody Map<String, Object> payload) {

        try {
            BigDecimal montant = new BigDecimal(payload.get("montant").toString());
            String iban = payload.get("iban").toString();

            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Le montant doit être positif");
                return ResponseEntity.badRequest().body(error);
            }

            boolean success = portefeuilleService.traiterRetrait(livreurId, montant, iban);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Retrait effectué avec succès");
                log.info("Retrait effectué: {}€ pour livreur {}", montant, livreurId);
            } else {
                response.put("success", false);
                response.put("message", "Solde insuffisant pour effectuer ce retrait");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du retrait: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/retraits/{livreurId}")
    public ResponseEntity<?> getHistoriqueRetraits(@PathVariable Integer livreurId) {
        try {
            List<Retrait> retraits = portefeuilleService.getHistoriqueRetraits(livreurId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("retraits", retraits);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
