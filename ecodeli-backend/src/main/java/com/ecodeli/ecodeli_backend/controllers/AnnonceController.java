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
import java.util.Map;
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

    @PutMapping("/{id}/demande-validation")
    public ResponseEntity<?> demanderValidation(@PathVariable Integer id, @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.demanderValidation(id, idLivreur);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/demande-validation-segment1")
    public ResponseEntity<?> demanderValidationSegment1(
            @PathVariable Integer id,
            @RequestParam Integer idLivreur,
            @RequestParam String entrepotVille) {
        try {
            Annonce annonce = annonceService.demanderValidationPartielle(id, idLivreur, entrepotVille, 1);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur");
        }
    }

    @PutMapping("/{id}/demande-validation-segment1-optimal")
    public ResponseEntity<?> demanderValidationSegment1Optimal(
            @PathVariable Integer id,
            @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.demanderValidationSegment1Optimal(id, idLivreur);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur");
        }
    }

    @PutMapping("/{id}/demande-validation-segment2")
    public ResponseEntity<?> demanderValidationSegment2(
            @PathVariable Integer id,
            @RequestParam Integer idLivreur,
            @RequestParam(required = false) String entrepotVille) {
        try {
            Annonce annonce = annonceService.demanderValidationPartielle(id, idLivreur, entrepotVille, 2);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/demande-validation-partielle")
    public ResponseEntity<?> demanderValidationPartielle(
            @PathVariable Integer id,
            @RequestParam Integer idLivreur,
            @RequestParam String entrepotVille,
            @RequestParam(defaultValue = "1") Integer numeroSegment) {
        try {
            Annonce annonce = annonceService.demanderValidationPartielle(id, idLivreur, entrepotVille, numeroSegment);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/livraison-partielle-autorisee")
    public ResponseEntity<List<Annonce>> getAnnoncesAvecLivraisonPartielleAutorisee() {
        List<Annonce> annonces = annonceService.getAnnoncesAvecLivraisonPartielleAutorisee();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/segments-disponibles")
    public ResponseEntity<List<Annonce>> getAnnoncesAvecSegmentsDisponibles() {
        List<Annonce> annonces = annonceService.getAnnoncesAvecSegmentsDisponibles();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/segment1-disponible")
    public ResponseEntity<List<Annonce>> getAnnoncesAvecSegment1Disponible() {
        List<Annonce> annonces = annonceService.getAnnoncesAvecSegment1Disponible();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/segment2-disponible")
    public ResponseEntity<List<Annonce>> getAnnoncesAvecSegment2Disponible() {
        List<Annonce> annonces = annonceService.getAnnoncesAvecSegment2Disponible();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/segments-complets")
    public ResponseEntity<List<Annonce>> getAnnoncesAvecSegmentsComplets() {
        List<Annonce> annonces = annonceService.getAnnoncesAvecSegmentsComplets();
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/disponibles-livreurs")
    public ResponseEntity<List<Annonce>> getAnnoncesDisponiblesPourLivreurs() {
        List<Annonce> annonces = annonceService.getAnnoncesDisponiblesPourLivreurs();
        return ResponseEntity.ok(annonces);
    }

    @PutMapping("/{id}/commencer-segment1")
    public ResponseEntity<?> commencerSegment1(@PathVariable Integer id, @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.commencerSegment(id, idLivreur, 1);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/terminer-segment1")
    public ResponseEntity<?> terminerSegment1(@PathVariable Integer id, @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.terminerSegment(id, idLivreur, 1);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/commencer-segment2")
    public ResponseEntity<?> commencerSegment2(@PathVariable Integer id, @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.commencerSegment(id, idLivreur, 2);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/terminer-segment2")
    public ResponseEntity<?> terminerSegment2(@PathVariable Integer id, @RequestParam Integer idLivreur) {
        try {
            Annonce annonce = annonceService.terminerSegment(id, idLivreur, 2);
            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mes-segments")
    public ResponseEntity<List<Annonce>> getMesSegments(@RequestParam Integer idLivreur) {
        List<Annonce> annonces = annonceService.getMesSegments(idLivreur);
        return ResponseEntity.ok(annonces);
    }

    @GetMapping("/{id}/statut-detaille")
    public ResponseEntity<?> getStatutDetaille(@PathVariable Integer id) {
        try {
            Map<String, Object> statut = annonceService.getStatutDetaille(id);
            return ResponseEntity.ok(statut);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
