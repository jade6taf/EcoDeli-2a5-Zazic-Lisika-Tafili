package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.services.AnnonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "*")
public class AnnonceController {

    @Autowired
    private AnnonceService annonceService;

    @GetMapping
    public ResponseEntity<List<Annonce>> getAllAnnonces() {
        List<Annonce> annonces = annonceService.getAllAnnonces();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annonce> getAnnonceById(@PathVariable Integer id) {
        Annonce annonce = annonceService.getAnnonceById(id);
        return ResponseEntity.ok(annonce);
    }

    @PostMapping
    public ResponseEntity<Annonce> createAnnonce(@RequestBody Map<String, Object> annonceData) {
        Annonce annonce = annonceService.createAnnonce(annonceData);
        return ResponseEntity.ok(annonce);
    }

    @PostMapping("/calculate-distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(@RequestBody Map<String, String> addresses) {
        String adresseDepart = addresses.get("adresseDepart");
        String adresseFin = addresses.get("adresseFin");

        BigDecimal distance = annonceService.calculateDistance(adresseDepart, adresseFin);
        BigDecimal prix = annonceService.calculatePrice(distance);

        Map<String, Object> response = new HashMap<>();
        response.put("distance", distance);
        response.put("prix", prix);
        response.put("tarifParKm", "0.80");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Annonce>> getAnnoncesByUser(@PathVariable Integer userId) {
        List<Annonce> annonces = annonceService.getAnnoncesByUser(userId);
        return ResponseEntity.ok(annonces);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAnnonce(@PathVariable Integer id) {
        annonceService.cancelAnnonce(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Annonce annulée avec succès");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Annonce>> getAvailableAnnonces() {
        List<Annonce> annonces = annonceService.getAvailableAnnonces();
        return ResponseEntity.ok(annonces);
    }

    @PutMapping("/{id}/take")
    public ResponseEntity<Map<String, Object>> takeAnnonce(@PathVariable Integer id, @RequestBody Map<String, Object> livreurData) {
        Integer livreurId = (Integer) livreurData.get("livreurId");
        annonceService.takeAnnonce(id, livreurId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Annonce prise en charge avec succès");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/start-delivery")
    public ResponseEntity<Map<String, Object>> startDelivery(@PathVariable Integer id) {
        annonceService.startDelivery(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Livraison commencée avec succès");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeDelivery(@PathVariable Integer id) {
        annonceService.completeDelivery(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Livraison terminée avec succès");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/livreur/{livreurId}")
    public ResponseEntity<List<Annonce>> getAnnoncesByLivreur(@PathVariable Integer livreurId) {
        List<Annonce> annonces = annonceService.getAnnoncesByLivreur(livreurId);
        return ResponseEntity.ok(annonces);
    }

    @PostMapping("/{id}/generate-code")
    public ResponseEntity<Map<String, Object>> generateDeliveryCode(@PathVariable Integer id) {
        try {
            String code = annonceService.generateDeliveryCode(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Code de validation généré et envoyé");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/validate-code")
    public ResponseEntity<Map<String, Object>> validateDeliveryCode(@PathVariable Integer id, @RequestBody Map<String, String> codeData) {
        try {
            String code = codeData.get("code");
            boolean isValid = annonceService.validateDeliveryCode(id, code);

            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                response.put("success", true);
                response.put("message", "Livraison terminée avec succès !");
            } else {
                response.put("success", false);
                response.put("message", "Code de validation incorrect");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/partielle/{id}/valider-livreurs")
    public ResponseEntity<Map<String, Object>> validerLivreursPartielle(@PathVariable Integer id) {
        try {
            annonceService.validerLivreursPartielle(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Les deux livreurs ont été validés. La livraison partielle peut commencer.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
