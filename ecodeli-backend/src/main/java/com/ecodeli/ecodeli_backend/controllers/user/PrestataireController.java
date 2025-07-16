package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Candidature;
import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.Justificatif;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.services.PrestataireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prestataire")
@CrossOrigin(origins = "*")
public class PrestataireController {

    @Autowired
    private PrestataireService prestataireService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer les demandes de service disponibles pour le prestataire connecté
     */
    @GetMapping("/demandes-disponibles")
    public ResponseEntity<List<DemandeService>> getDemandesDisponibles(Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            List<DemandeService> demandes = prestataireService.getDemandesDisponibles(prestataireId);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statut-validation")
    public ResponseEntity<Map<String, Object>> getStatutValidation(Authentication authentication) {
        try {
            String identifier = authentication.getName();
            Integer prestataireId;

            if (identifier != null && identifier.contains("@")) {
                Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(identifier);
                if (optionalUser.isEmpty() || !(optionalUser.get() instanceof Prestataire)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Prestataire non trouvé");
                    return ResponseEntity.status(404).body(error);
                }
                prestataireId = optionalUser.get().getIdUtilisateur();
            } else {
                prestataireId = Integer.parseInt(identifier);
            }
            
            Map<String, Object> statut = prestataireService.getStatutValidationDetaille(prestataireId);
            return ResponseEntity.ok(statut);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/candidater")
    public ResponseEntity<Map<String, Object>> creerCandidature(
            Authentication authentication,
            @RequestBody Map<String, Object> candidatureData) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            
            Long demandeId = ((Number) candidatureData.get("demandeId")).longValue();
            String messagePersonnalise = (String) candidatureData.get("messagePersonnalise");
            Integer delaiPropose = candidatureData.get("delaiPropose") != null ? 
                ((Number) candidatureData.get("delaiPropose")).intValue() : null;

            Candidature candidature = prestataireService.creerCandidature(
                prestataireId, demandeId, messagePersonnalise, delaiPropose);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidature envoyée avec succès");
            response.put("candidature", candidature);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/mes-candidatures")
    public ResponseEntity<List<Candidature>> getMesCandidatures(Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            List<Candidature> candidatures = prestataireService.getCandidaturesPrestataire(prestataireId);
            return ResponseEntity.ok(candidatures);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques(Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            Map<String, Object> stats = prestataireService.getStatistiquesPrestataire(prestataireId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/demandes/{demandeId}")
    public ResponseEntity<Map<String, Object>> getDemandeDetails(
            @PathVariable Long demandeId,
            Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            
            if (!prestataireService.isPrestataireValide(prestataireId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Prestataire non validé");
                return ResponseEntity.status(403).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Détails de la demande");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/peut-candidater/{demandeId}")
    public ResponseEntity<Map<String, Object>> peutCandidater(
            @PathVariable Long demandeId,
            Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            
            Map<String, Object> response = new HashMap<>();

            boolean estValide = prestataireService.isPrestataireValide(prestataireId);
            response.put("estValide", estValide);
            
            if (!estValide) {
                response.put("message", "Prestataire non validé");
                response.put("peutCandidater", false);
            } else {
                response.put("peutCandidater", true);
                response.put("message", "Peut candidater");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profil")
    public ResponseEntity<Map<String, Object>> getProfil(Authentication authentication) {
        try {
            Integer prestataireId = Integer.parseInt(authentication.getName());
            
            Map<String, Object> profil = new HashMap<>();

            Map<String, Object> statutValidation = prestataireService.getStatutValidationDetaille(prestataireId);
            profil.put("validation", statutValidation);
            
            Map<String, Object> statistiques = prestataireService.getStatistiquesPrestataire(prestataireId);
            profil.put("statistiques", statistiques);

            return ResponseEntity.ok(profil);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload-justificatif")
    public ResponseEntity<Map<String, Object>> uploadJustificatif(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        try {
            Integer prestataireId = getPrestataireId(authentication);
            
            Justificatif justificatif = prestataireService.uploadJustificatif(prestataireId, file, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Justificatif uploadé avec succès");
            response.put("justificatif", justificatif);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/justificatifs")
    public ResponseEntity<List<Justificatif>> getJustificatifs(Authentication authentication) {
        try {
            Integer prestataireId = getPrestataireId(authentication);
            List<Justificatif> justificatifs = prestataireService.getJustificatifs(prestataireId);
            return ResponseEntity.ok(justificatifs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/justificatifs/{justificatifId}")
    public ResponseEntity<Map<String, Object>> supprimerJustificatif(
            @PathVariable Long justificatifId,
            Authentication authentication) {
        try {
            Integer prestataireId = getPrestataireId(authentication);
            prestataireService.supprimerJustificatif(prestataireId, justificatifId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Justificatif supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/demandes-disponibles/paginated")
    public ResponseEntity<Page<DemandeService>> getDemandesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateMin,
            @RequestParam(required = false) String dateMax,
            @RequestParam(required = false) String localisation,
            Authentication authentication) {
        try {
            Integer prestataireId = getPrestataireId(authentication);
            Page<DemandeService> demandes = prestataireService.getDemandesPaginated(
                prestataireId, page, size, search, dateMin, dateMax, localisation);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Integer getPrestataireId(Authentication authentication) {
        String identifier = authentication.getName();
        
        if (identifier != null && identifier.contains("@")) {
            Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(identifier);
            if (optionalUser.isEmpty() || !(optionalUser.get() instanceof Prestataire)) {
                throw new RuntimeException("Prestataire non trouvé");
            }
            return optionalUser.get().getIdUtilisateur();
        } else {
            return Integer.parseInt(identifier);
        }
    }
}