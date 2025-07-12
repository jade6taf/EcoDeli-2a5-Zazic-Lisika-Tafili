package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.CandidatureLivraison;
import com.ecodeli.ecodeli_backend.services.CandidatureLivraisonService;
import com.ecodeli.ecodeli_backend.services.EntrepotService;
import com.ecodeli.ecodeli_backend.dto.request.CandidaturePartielleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures-livraison")
@CrossOrigin(origins = "*")
public class CandidatureLivraisonController {

    @Autowired
    private CandidatureLivraisonService candidatureLivraisonService;

    @Autowired
    private EntrepotService entrepotService;

    @PostMapping
    public ResponseEntity<CandidatureLivraison> postuler(@RequestBody Map<String, Object> candidatureData) {
        CandidatureLivraison candidature = candidatureLivraisonService.postuler(candidatureData);
        return ResponseEntity.ok(candidature);
    }

    @GetMapping("/annonce/{annonceId}")
    public ResponseEntity<List<CandidatureLivraison>> getCandidaturesByAnnonce(@PathVariable Integer annonceId) {
        List<CandidatureLivraison> candidatures = candidatureLivraisonService.getCandidaturesByAnnonce(annonceId);
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/livreur/{livreurId}")
    public ResponseEntity<List<CandidatureLivraison>> getCandidaturesByLivreur(@PathVariable Integer livreurId) {
        List<CandidatureLivraison> candidatures = candidatureLivraisonService.getCandidaturesByLivreur(livreurId);
        return ResponseEntity.ok(candidatures);
    }

    @PutMapping("/{candidatureId}/accepter")
    public ResponseEntity<Map<String, Object>> accepterCandidature(
            @PathVariable Long candidatureId,
            @RequestBody Map<String, Object> data) {
        String commentaire = (String) data.get("commentaire");
        candidatureLivraisonService.accepterCandidature(candidatureId, commentaire);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Candidature acceptée avec succès");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{candidatureId}/refuser")
    public ResponseEntity<Map<String, Object>> refuserCandidature(
            @PathVariable Long candidatureId,
            @RequestBody Map<String, Object> data) {
        String commentaire = (String) data.get("commentaire");
        candidatureLivraisonService.refuserCandidature(candidatureId, commentaire);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Candidature refusée avec succès");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{candidatureId}")
    public ResponseEntity<CandidatureLivraison> getCandidatureById(@PathVariable Long candidatureId) {
        CandidatureLivraison candidature = candidatureLivraisonService.getCandidatureById(candidatureId);
        return ResponseEntity.ok(candidature);
    }

    @PostMapping("/partielle")
    public ResponseEntity<Map<String, Object>> candidaterPartielle(@Valid @RequestBody CandidaturePartielleRequest request) {
        try {
            if (!request.isValid()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Entrepôt obligatoire pour le segment 1");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getEntrepotChoisi() != null && !entrepotService.entrepotExiste(request.getEntrepotChoisi())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Entrepôt non valide");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            CandidatureLivraison candidature = candidatureLivraisonService.candidaterPartielle(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidature partielle créée avec succès");
            response.put("candidature", candidature);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/entrepots")
    public ResponseEntity<List<EntrepotService.EntrepotDTO>> getEntrepotsDisponibles() {
        List<EntrepotService.EntrepotDTO> entrepots = entrepotService.getEntrepotsDisponibles();
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/annonce/{annonceId}/segments")
    public ResponseEntity<Map<String, Object>> getCandidaturesParSegment(@PathVariable Integer annonceId) {
        Map<String, Object> candidaturesParSegment = candidatureLivraisonService.getCandidaturesParSegment(annonceId);
        return ResponseEntity.ok(candidaturesParSegment);
    }

    @PutMapping("/{candidatureId}/accepter-partielle")
    public ResponseEntity<Map<String, Object>> accepterCandidaturePartielle(
            @PathVariable Long candidatureId,
            @RequestBody Map<String, Object> data) {
        try {
            String commentaire = (String) data.get("commentaire");
            candidatureLivraisonService.accepterCandidaturePartielle(candidatureId, commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidature partielle acceptée avec succès");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
