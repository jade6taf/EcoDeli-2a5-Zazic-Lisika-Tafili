package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Entreprise;
import com.ecodeli.ecodeli_backend.services.EntrepriseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entreprises")
@CrossOrigin(origins = "http://localhost:5173")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping
    public ResponseEntity<List<Entreprise>> getAllEntreprises() {
        List<Entreprise> entreprises = entrepriseService.getAllEntreprises();
        return ResponseEntity.ok(entreprises);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entreprise> getEntrepriseById(@PathVariable Integer id) {
        Optional<Entreprise> entreprise = entrepriseService.getEntrepriseById(id);
        return entreprise.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/siret/{siret}")
    public ResponseEntity<Entreprise> getEntrepriseBySiret(@PathVariable String siret) {
        Optional<Entreprise> entreprise = entrepriseService.getEntrepriseBySiret(siret);
        return entreprise.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // @GetMapping("/statut/{statutJuridique}")
    // public ResponseEntity<List<Entreprise>> getEntreprisesByStatutJuridique(@PathVariable StatutJuridique statutJuridique) {
    //     List<Entreprise> entreprises = entrepriseService.getEntreprisesByStatutJuridique(statutJuridique);
    //     return ResponseEntity.ok(entreprises);
    // }

    @GetMapping("/secteur/{secteur}")
    public ResponseEntity<List<Entreprise>> getEntreprisesBySecteurActivite(@PathVariable String secteur) {
        List<Entreprise> entreprises = entrepriseService.getEntreprisesBySecteurActivite(secteur);
        return ResponseEntity.ok(entreprises);
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Entreprise>> getEntreprisesByVille(@PathVariable String ville) {
        List<Entreprise> entreprises = entrepriseService.getEntreprisesByVille(ville);
        return ResponseEntity.ok(entreprises);
    }

    @GetMapping("/validation/{validated}")
    public ResponseEntity<List<Entreprise>> getEntreprisesByValidationStatus(@PathVariable Boolean validated) {
        List<Entreprise> entreprises = entrepriseService.getEntreprisesByValidationStatus(validated);
        return ResponseEntity.ok(entreprises);
    }

    @PostMapping
    public ResponseEntity<?> createEntreprise(@RequestBody Entreprise entreprise) {
        try {
            Entreprise newEntreprise = entrepriseService.createEntreprise(entreprise);
            return new ResponseEntity<>(newEntreprise, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntreprise(@PathVariable Integer id, @RequestBody Entreprise entreprise) {
        try {
            Entreprise updatedEntreprise = entrepriseService.updateEntreprise(id, entreprise);
            return ResponseEntity.ok(updatedEntreprise);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<?> validateEntreprise(@PathVariable Integer id) {
        try {
            Entreprise validatedEntreprise = entrepriseService.validateEntreprise(id);
            return ResponseEntity.ok(validatedEntreprise);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntreprise(@PathVariable Integer id) {
        try {
            entrepriseService.deleteEntreprise(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}