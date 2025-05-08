package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prestataires")
@CrossOrigin(origins = "http://localhost:5173")
public class PrestataireController {

    private final UtilisateurService utilisateurService;

    public PrestataireController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/services-types")
    public ResponseEntity<List<Map<String, String>>> getServicesTypes() {
        List<Map<String, String>> services = Arrays.stream(ServiceType.values())
            .map(type -> Map.of(
                "code", type.name(),
                "libelle", type.getLibelle()
            ))
            .collect(Collectors.toList());
        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurService.getUtilisateurByEmail(email)
                .map(user -> {
                    if (user instanceof Prestataire) {
                        return new ResponseEntity<>(user, HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Utilisateur non autorisé", HttpStatus.FORBIDDEN);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Prestataire prestataireDetails) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurService.getUtilisateurByEmail(email)
                .map(user -> {
                    if (user instanceof Prestataire) {
                        Prestataire prestataire = (Prestataire) user;
                        if (prestataireDetails.getDomaineExpertise() != null)
                            prestataire.setDomaineExpertise(prestataireDetails.getDomaineExpertise());
                        if (prestataireDetails.getZoneIntervention() != null)
                            prestataire.setZoneIntervention(prestataireDetails.getZoneIntervention());
                        if (prestataireDetails.getDisponible() != null)
                            prestataire.setDisponible(prestataireDetails.getDisponible());
                        if (prestataireDetails.getTarifHoraire() != null)
                            prestataire.setTarifHoraire(prestataireDetails.getTarifHoraire());
                        if (prestataireDetails.getDescription() != null)
                            prestataire.setDescription(prestataireDetails.getDescription());
                        if (prestataireDetails.getImageUrl() != null)
                            prestataire.setImageUrl(prestataireDetails.getImageUrl());

                        Prestataire updated = (Prestataire) utilisateurService.updateUtilisateur(prestataire.getIdUtilisateur(), prestataire);
                        return new ResponseEntity<>(updated, HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Utilisateur non autorisé", HttpStatus.FORBIDDEN);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPrestataire(@PathVariable Integer id) {
        return utilisateurService.getUtilisateurById(id)
                .map(user -> {
                    if (user instanceof Prestataire) {
                        return new ResponseEntity<>(user, HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Utilisateur non trouvé", HttpStatus.NOT_FOUND);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/by-service/{serviceType}")
    public ResponseEntity<?> getPrestatairesByServiceType(@PathVariable ServiceType serviceType) {
        List<Prestataire> prestataires = utilisateurService.getAllUtilisateurs().stream()
            .filter(user -> user instanceof Prestataire)
            .map(user -> (Prestataire) user)
            .filter(prestataire -> prestataire.getDomaineExpertise() == serviceType
                && (prestataire.getDisponible() != null && prestataire.getDisponible()))
            .collect(Collectors.toList());

        if (prestataires.isEmpty()) {
            return new ResponseEntity<>("Aucun prestataire trouvé pour ce type de service", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(prestataires, HttpStatus.OK);
    }
}
