package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.services.ColisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/colis")
@CrossOrigin(origins = "http://localhost:5173")
public class ColisController {

    private final ColisService colisService;

    public ColisController(ColisService colisService) {
        this.colisService = colisService;
    }

    @GetMapping
    public ResponseEntity<List<Colis>> getAllColis() {
        List<Colis> colis = colisService.getAllColis();
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Colis> getColisById(@PathVariable Integer id) {
        Optional<Colis> colis = colisService.getColisById(id);
        return colis.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/expediteur/{idExpediteur}")
    public ResponseEntity<List<Colis>> getColisByExpediteur(@PathVariable Integer idExpediteur) {
        List<Colis> colis = colisService.getColisByExpediteur(idExpediteur);
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/destinataire/{idDestinataire}")
    public ResponseEntity<List<Colis>> getColisByDestinataire(@PathVariable Integer idDestinataire) {
        List<Colis> colis = colisService.getColisByDestinataire(idDestinataire);
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Colis>> getColisByType(@PathVariable String type) {
        List<Colis> colis = colisService.getColisByType(type);
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/recherche/poids")
    public ResponseEntity<List<Colis>> searchColisByPoidRange(
            @RequestParam BigDecimal poidMin,
            @RequestParam BigDecimal poidMax) {
        List<Colis> colis = colisService.searchColisByPoidRange(poidMin, poidMax);
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/recherche/taille")
    public ResponseEntity<List<Colis>> searchColisByTailleRange(
            @RequestParam Integer tailleMin,
            @RequestParam Integer tailleMax) {
        List<Colis> colis = colisService.searchColisByTailleRange(tailleMin, tailleMax);
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<Colis>> getActivePackages() {
        List<Colis> colis = colisService.getActivePackages();
        return ResponseEntity.ok(colis);
    }

    @PostMapping
    public ResponseEntity<?> createColis(
            @RequestBody Colis colis,
            @RequestParam Integer idExpediteur,
            @RequestParam Integer idDestinataire) {
        try {
            Colis newColis = colisService.createColis(colis, idExpediteur, idDestinataire);
            return new ResponseEntity<>(newColis, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColis(@PathVariable Integer id, @RequestBody Colis colis) {
        try {
            Colis updatedColis = colisService.updateColis(id, colis);
            return ResponseEntity.ok(updatedColis);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/destinataire/{idNouveauDestinataire}")
    public ResponseEntity<?> changeDestinataire(
            @PathVariable Integer id,
            @PathVariable Integer idNouveauDestinataire) {
        try {
            Colis updatedColis = colisService.changeDestinataire(id, idNouveauDestinataire);
            return ResponseEntity.ok(updatedColis);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColis(@PathVariable Integer id) {
        try {
            colisService.deleteColis(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}