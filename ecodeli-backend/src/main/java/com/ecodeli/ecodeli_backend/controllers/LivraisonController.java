package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.services.LivraisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livraisons")
@CrossOrigin(origins = "http://localhost:5173")
public class LivraisonController {

    private final LivraisonService livraisonService;

    public LivraisonController(LivraisonService livraisonService) {
        this.livraisonService = livraisonService;
    }

    @GetMapping("/livreur/{idLivreur}")
    public ResponseEntity<List<Livraison>> getLivraisonsByLivreur(@PathVariable Integer idLivreur) {
        List<Livraison> livraisons = livraisonService.getLivraisonsByLivreur(idLivreur);
        return ResponseEntity.ok(livraisons);
    }

    @PutMapping("/{id}/demarrer")
    public ResponseEntity<?> demarrerLivraison(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.demarrerLivraison(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<?> terminerLivraison(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.terminerLivraison(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> annulerLivraison(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.annulerLivraison(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/arriver")
    public ResponseEntity<?> arriverALivraison(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.arriverALivraison(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirmer-otp")
    public ResponseEntity<?> confirmerLivraisonParOtp(@PathVariable Integer id, @RequestBody OtpRequest otpRequest) {
        try {
            Livraison livraison = livraisonService.confirmerLivraisonParOtp(id, otpRequest.getOtp());
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attente-segment-2")
    public ResponseEntity<List<Livraison>> getLivraisonsEnAttenteSegment2() {
        List<Livraison> livraisons = livraisonService.getLivraisonsEnAttenteSegment2();
        return ResponseEntity.ok(livraisons);
    }

    @GetMapping("/attente-segment-2/{ville}")
    public ResponseEntity<List<Livraison>> getLivraisonsEnAttenteSegment2ParVille(@PathVariable String ville) {
        List<Livraison> livraisons = livraisonService.getLivraisonsEnAttenteSegment2ParVille(ville);
        return ResponseEntity.ok(livraisons);
    }

    @PutMapping("/{id}/terminer-segment-1")
    public ResponseEntity<?> terminerSegment1(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.terminerSegment1(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/demarrer-segment-2/{idLivreur}")
    public ResponseEntity<?> demarrerSegment2(@PathVariable Integer id, @PathVariable Integer idLivreur) {
        try {
            Livraison livraison = livraisonService.demarrerSegment2(id, idLivreur);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/arriver-segment-2")
    public ResponseEntity<?> arriverSegment2(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.arriverSegment2(id);
            return ResponseEntity.ok(livraison);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
