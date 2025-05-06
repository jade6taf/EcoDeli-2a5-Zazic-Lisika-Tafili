package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prestataires")
@CrossOrigin(origins = "http://localhost:5173")
public class PrestataireController {

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


    private final UtilisateurService utilisateurService;

    public PrestataireController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
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

}
