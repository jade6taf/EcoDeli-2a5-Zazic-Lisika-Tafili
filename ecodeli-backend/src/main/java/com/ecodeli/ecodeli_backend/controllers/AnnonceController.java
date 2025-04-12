package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.models.Annonce.TypeAnnonce;
import com.ecodeli.ecodeli_backend.services.AnnonceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin(origins = "http://localhost:5173")
public class AnnonceController {

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GetMapping
    public ResponseEntity<List<Annonce>> getAllAnnonces() {
        List<Annonce> annonces = annonceService.getAllAnnonces();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annonce> getAnnonceById(@PathVariable Integer id) {
        Optional<Annonce> annonce = annonceService.getAnnonceById(id);
        return annonce.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Annonce>> getAnnoncesByType(@PathVariable TypeAnnonce type) {
        List<Annonce> annonces = annonceService.getAnnoncesByType(type);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Annonce>> getAnnoncesByStatut(@PathVariable StatutAnnonce statut) {
        List<Annonce> annonces = annonceService.getAnnoncesByStatut(statut);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/expediteur/{id}")
    public ResponseEntity<List<Annonce>> getAnnoncesByExpediteur(@PathVariable Integer id) {
        List<Annonce> annonces = annonceService.getAnnoncesByExpediteur(id);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/recherche/depart/{ville}")
    public ResponseEntity<List<Annonce>> searchAnnoncesByDepartureCity(@PathVariable String ville) {
        List<Annonce> annonces = annonceService.searchAnnoncesByDepartureCity(ville);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/recherche/arrivee/{ville}")
    public ResponseEntity<List<Annonce>> searchAnnoncesByArrivalCity(@PathVariable String ville) {
        List<Annonce> annonces = annonceService.searchAnnoncesByArrivalCity(ville);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/recherche/prix")
    public ResponseEntity<List<Annonce>> searchAnnoncesByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<Annonce> annonces = annonceService.searchAnnoncesByPriceRange(min, max);
        return ResponseEntity.ok(annonces);
    }

    @PostMapping
    public ResponseEntity<?> createAnnonce(
            @RequestBody Annonce annonce,
            @RequestParam Integer idExpediteur) {
        try {
            Annonce newAnnonce = annonceService.createAnnonce(annonce, idExpediteur);
            return new ResponseEntity<>(newAnnonce, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnonce(@PathVariable Integer id, @RequestBody Annonce annonce) {
        try {
            Annonce updatedAnnonce = annonceService.updateAnnonce(id, annonce);
            return ResponseEntity.ok(updatedAnnonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> cancelAnnonce(@PathVariable Integer id) {
        try {
            Annonce canceledAnnonce = annonceService.cancelAnnonce(id);
            return ResponseEntity.ok(canceledAnnonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnonce(@PathVariable Integer id) {
        try {
            annonceService.deleteAnnonce(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}