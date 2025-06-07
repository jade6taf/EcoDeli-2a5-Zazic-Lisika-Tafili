package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.services.AnnonceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commercants")
@CrossOrigin(origins = "http://localhost:5173")
public class CommercantController {

    private final AnnonceService annonceService;

    public CommercantController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GetMapping("/{idCommercant}/annonces")
    public ResponseEntity<List<Annonce>> getAnnoncesByCommercant(@PathVariable Integer idCommercant) {
        try {
            List<Annonce> annonces = annonceService.getAnnoncesByCommercant(idCommercant);
            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{idCommercant}/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesCommercant(@PathVariable Integer idCommercant) {
        try {
            Map<String, Object> statistiques = annonceService.getStatistiquesCommercant(idCommercant);
            return ResponseEntity.ok(statistiques);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{idCommercant}/annonces")
    public ResponseEntity<?> createAnnonceCommercant(
            @PathVariable Integer idCommercant,
            @Valid @RequestBody Annonce annonce) {
        try {
            Annonce nouvelleAnnonce = annonceService.createAnnonceCommercant(annonce, idCommercant);
            return new ResponseEntity<>(nouvelleAnnonce, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Erreur lors de la création de l'annonce"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{idCommercant}/annonces/statut/{statut}")
    public ResponseEntity<List<Annonce>> getAnnoncesByCommercantAndStatut(
            @PathVariable Integer idCommercant,
            @PathVariable StatutAnnonce statut) {
        try {
            List<Annonce> annonces = annonceService.getAnnoncesByCommercantAndStatut(idCommercant, statut);
            return ResponseEntity.ok(annonces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{idCommercant}/annonces/{idAnnonce}")
    public ResponseEntity<?> updateAnnonceCommercant(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idAnnonce,
            @Valid @RequestBody Annonce annonceDetails) {
        try {
            Annonce annonceExistante = annonceService.getAnnonceById(idAnnonce)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (!annonceExistante.getExpediteur().getIdUtilisateur().equals(idCommercant)) {
                return new ResponseEntity<>(Map.of("message", "Cette annonce ne vous appartient pas"), HttpStatus.FORBIDDEN);
            }

            Annonce annonceModifiee = annonceService.updateAnnonce(idAnnonce, annonceDetails);
            return ResponseEntity.ok(annonceModifiee);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Erreur lors de la modification de l'annonce"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{idCommercant}/annonces/{idAnnonce}/annuler")
    public ResponseEntity<?> cancelAnnonceCommercant(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idAnnonce) {
        try {
            Annonce annonceExistante = annonceService.getAnnonceById(idAnnonce)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (!annonceExistante.getExpediteur().getIdUtilisateur().equals(idCommercant)) {
                return new ResponseEntity<>(Map.of("message", "Cette annonce ne vous appartient pas"), HttpStatus.FORBIDDEN);
            }

            Annonce annonceAnnulee = annonceService.cancelAnnonce(idAnnonce);
            return ResponseEntity.ok(annonceAnnulee);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Erreur lors de l'annulation de l'annonce"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{idCommercant}/annonces/{idAnnonce}")
    public ResponseEntity<?> deleteAnnonceCommercant(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idAnnonce) {
        try {
            Annonce annonceExistante = annonceService.getAnnonceById(idAnnonce)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (!annonceExistante.getExpediteur().getIdUtilisateur().equals(idCommercant)) {
                return new ResponseEntity<>(Map.of("message", "Cette annonce ne vous appartient pas"), HttpStatus.FORBIDDEN);
            }

            if (annonceExistante.getStatut() != StatutAnnonce.PUBLIEE) {
                return new ResponseEntity<>(Map.of("message", "Seules les annonces publiées peuvent être supprimées"), HttpStatus.BAD_REQUEST);
            }

            annonceService.deleteAnnonce(idAnnonce);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Erreur lors de la suppression de l'annonce"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{idCommercant}/annonces/{idAnnonce}")
    public ResponseEntity<?> getAnnonceCommercantById(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idAnnonce) {
        try {
            Annonce annonce = annonceService.getAnnonceById(idAnnonce)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (!annonce.getExpediteur().getIdUtilisateur().equals(idCommercant)) {
                return new ResponseEntity<>(Map.of("message", "Cette annonce ne vous appartient pas"), HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(annonce);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{idCommercant}/annonces/{idAnnonce}/statut")
    public ResponseEntity<?> getStatutDetailleAnnonce(
            @PathVariable Integer idCommercant,
            @PathVariable Integer idAnnonce) {
        try {
            Annonce annonceExistante = annonceService.getAnnonceById(idAnnonce)
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));

            if (!annonceExistante.getExpediteur().getIdUtilisateur().equals(idCommercant)) {
                return new ResponseEntity<>(Map.of("message", "Cette annonce ne vous appartient pas"), HttpStatus.FORBIDDEN);
            }

            Map<String, Object> statutDetaille = annonceService.getStatutDetaille(idAnnonce);
            return ResponseEntity.ok(statutDetaille);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
