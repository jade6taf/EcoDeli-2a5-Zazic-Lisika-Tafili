package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("photo") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return utilisateurService.getUtilisateurByEmail(email)
                .map(user -> {
                    if (!(user instanceof Prestataire)) {
                        return new ResponseEntity<>("Utilisateur non autorisé", HttpStatus.FORBIDDEN);
                    }
                    Prestataire prestataire = (Prestataire) user;
                    if (file.isEmpty()) {
                        return new ResponseEntity<>("Fichier vide", HttpStatus.BAD_REQUEST);
                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return new ResponseEntity<>("Le fichier doit être une image", HttpStatus.BAD_REQUEST);
                    }
                    if (file.getSize() > 5 * 1024 * 1024) {
                        return new ResponseEntity<>("Le fichier ne doit pas dépasser 5MB", HttpStatus.BAD_REQUEST);
                    }
                    try {
                        Path uploadDir = Paths.get("uploads/profile-photos");
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectories(uploadDir);
                        }
                        String originalFileName = file.getOriginalFilename();
                        String fileExtension = originalFileName != null && originalFileName.contains(".")
                            ? originalFileName.substring(originalFileName.lastIndexOf("."))
                            : ".jpg";
                        String fileName = "prestataire_" + prestataire.getIdUtilisateur() + "_" +
                                         System.currentTimeMillis() + fileExtension;

                        Path filePath = uploadDir.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        String imageUrl = "/uploads/profile-photos/" + fileName;

                        prestataire.setImageUrl(imageUrl);
                        Prestataire updated = (Prestataire) utilisateurService.updateUtilisateur(
                            prestataire.getIdUtilisateur(), prestataire);

                        Map<String, String> response = new HashMap<>();
                        response.put("imageUrl", imageUrl);
                        response.put("message", "Photo de profil mise à jour avec succès");

                        return new ResponseEntity<>(response, HttpStatus.OK);

                    } catch (IOException e) {
                        return new ResponseEntity<>("Erreur lors de la sauvegarde du fichier", 
                                                  HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/disponibilite")
    public ResponseEntity<?> updateDisponibilite(@RequestBody Map<String, Boolean> requestBody) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Boolean disponible = requestBody.get("disponible");

        if (disponible == null) {
            return new ResponseEntity<>("Le paramètre 'disponible' est requis", HttpStatus.BAD_REQUEST);
        }

        return utilisateurService.getUtilisateurByEmail(email)
                .map(user -> {
                    if (!(user instanceof Prestataire)) {
                        return new ResponseEntity<>("Utilisateur non autorisé", HttpStatus.FORBIDDEN);
                    }

                    Prestataire prestataire = (Prestataire) user;
                    prestataire.setDisponible(disponible);

                    Prestataire updated = (Prestataire) utilisateurService.updateUtilisateur(
                        prestataire.getIdUtilisateur(), prestataire);

                    Map<String, Object> response = new HashMap<>();
                    response.put("disponible", updated.getDisponible());
                    response.put("message", disponible ? "Vous êtes maintenant disponible" : 
                                          "Vous êtes maintenant indisponible");

                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
