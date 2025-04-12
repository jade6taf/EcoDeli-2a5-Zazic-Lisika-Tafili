package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Entrepot;
import com.ecodeli.ecodeli_backend.models.Entrepot.StatutEntrepot;
import com.ecodeli.ecodeli_backend.services.EntrepotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/entrepots")
@CrossOrigin(origins = "http://localhost:5173")
public class EntrepotController {

    private final EntrepotService entrepotService;

    public EntrepotController(EntrepotService entrepotService) {
        this.entrepotService = entrepotService;
    }

    @GetMapping
    public ResponseEntity<List<Entrepot>> getAllEntrepots() {
        List<Entrepot> entrepots = entrepotService.getAllEntrepots();
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrepot> getEntrepotById(@PathVariable Integer id) {
        Optional<Entrepot> entrepot = entrepotService.getEntrepotById(id);
        return entrepot.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Entrepot>> getEntrepotsByStatut(@PathVariable StatutEntrepot statut) {
        List<Entrepot> entrepots = entrepotService.getEntrepotsByStatut(statut);
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Entrepot>> getEntrepotsByVille(@PathVariable String ville) {
        List<Entrepot> entrepots = entrepotService.getEntrepotsByVille(ville);
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/actifs/ville/{ville}")
    public ResponseEntity<List<Entrepot>> getActiveEntrepotsByVille(@PathVariable String ville) {
        List<Entrepot> entrepots = entrepotService.getActiveEntrepotsByVille(ville);
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/recherche/places")
    public ResponseEntity<List<Entrepot>> searchEntrepotsByPlacesRange(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<Entrepot> entrepots = entrepotService.searchEntrepotsByPlacesRange(min, max);
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/places/total")
    public ResponseEntity<Map<String, Integer>> calculateTotalAvailablePlaces() {
        Integer totalPlaces = entrepotService.calculateTotalAvailablePlaces();
        return ResponseEntity.ok(Map.of("totalPlaces", totalPlaces != null ? totalPlaces : 0));
    }

    @PostMapping
    public ResponseEntity<?> createEntrepot(@RequestBody Entrepot entrepot) {
        try {
            Entrepot newEntrepot = entrepotService.createEntrepot(entrepot);
            return new ResponseEntity<>(newEntrepot, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntrepot(@PathVariable Integer id, @RequestBody Entrepot entrepot) {
        try {
            Entrepot updatedEntrepot = entrepotService.updateEntrepot(id, entrepot);
            return ResponseEntity.ok(updatedEntrepot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/statut/{statut}")
    public ResponseEntity<?> changeStatut(@PathVariable Integer id, @PathVariable StatutEntrepot statut) {
        try {
            Entrepot updatedEntrepot = entrepotService.changeStatut(id, statut);
            return ResponseEntity.ok(updatedEntrepot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/augmenter")
    public ResponseEntity<?> augmenterCapacite(
            @PathVariable Integer id,
            @RequestParam Integer places) {
        try {
            Entrepot updatedEntrepot = entrepotService.augmenterCapacite(id, places);
            return ResponseEntity.ok(updatedEntrepot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/diminuer")
    public ResponseEntity<?> diminuerCapacite(
            @PathVariable Integer id,
            @RequestParam Integer places) {
        try {
            Entrepot updatedEntrepot = entrepotService.diminuerCapacite(id, places);
            return ResponseEntity.ok(updatedEntrepot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntrepot(@PathVariable Integer id) {
        try {
            entrepotService.deleteEntrepot(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}