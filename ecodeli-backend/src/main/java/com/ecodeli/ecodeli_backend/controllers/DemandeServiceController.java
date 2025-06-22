package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.DemandeService.StatutDemande;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.models.Client;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.services.DemandeServiceService;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes-services")
@CrossOrigin(origins = "*")
public class DemandeServiceController {

    @Autowired
    private DemandeServiceService demandeServiceService;

    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<?> creerDemande(@Valid @RequestBody DemandeService demande, Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!(utilisateur instanceof Client)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les clients peuvent créer des demandes de service"));
            }
            Client client = (Client) utilisateur;
            if (!demandeServiceService.peutCreerNouvelleDemande(client)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Vous avez atteint le nombre maximum de demandes actives (5)"));
            }

            demande.setClient(client);
            DemandeService nouvelleDemande = demandeServiceService.creerDemande(demande);

            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleDemande);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création de la demande"));
        }
    }

    @GetMapping
    public ResponseEntity<List<DemandeService>> getDemandesDisponibles() {
        try {
            List<DemandeService> demandes = demandeServiceService.getDemandesDisponiblesPourCandidatures();
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/mes-demandes")
    public ResponseEntity<?> getMesDemandes(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            if (!(utilisateur instanceof Client)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les clients peuvent consulter leurs demandes"));
            }
            Client client = (Client) utilisateur;
            List<DemandeService> demandes = demandeServiceService.getDemandesClient(client);
            return ResponseEntity.ok(demandes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des demandes"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDemandeById(@PathVariable Long id) {
        try {
            DemandeService demande = demandeServiceService.getDemandeById(id);
            return ResponseEntity.ok(demande);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération de la demande"));
        }
    }

    @GetMapping("/{id}/editable")
    public ResponseEntity<?> isDemandeEditable(@PathVariable Long id, Authentication authentication) {
        try {
            DemandeService demande = demandeServiceService.getDemandeById(id);
            String email = authentication.getName();

            if (!demande.getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez consulter que vos propres demandes"));
            }

            boolean editable = demande.peutEtreModifiee();
            return ResponseEntity.ok(Map.of(
                "editable", editable,
                "statut", demande.getStatut().name(),
                "raison", editable ? "Modification autorisée" : "La demande ne peut plus être modifiée dans son état actuel"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la vérification"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierDemande(@PathVariable Long id,
                                           @Valid @RequestBody DemandeService demandeModifiee,
                                           Authentication authentication) {
        try {
            DemandeService demandeExistante = demandeServiceService.getDemandeById(id);
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!demandeExistante.getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez modifier que vos propres demandes"));
            }

            DemandeService demandeModifieeResult = demandeServiceService.modifierDemande(id, demandeModifiee);
            return ResponseEntity.ok(demandeModifieeResult);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la modification de la demande"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> annulerDemande(@PathVariable Long id, Authentication authentication) {
        try {
            DemandeService demande = demandeServiceService.getDemandeById(id);
            String email = authentication.getName();

            if (!demande.getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez annuler que vos propres demandes"));
            }

            demandeServiceService.annulerDemande(id);
            return ResponseEntity.ok(Map.of("message", "Demande annulée avec succès"));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de l'annulation de la demande"));
        }
    }

    @GetMapping("/categorie/{categorieService}")
    public ResponseEntity<List<DemandeService>> getDemandesParCategorie(@PathVariable ServiceType categorieService) {
        try {
            List<DemandeService> demandes = demandeServiceService.getDemandesParCategorie(categorieService);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DemandeService>> getDemandesParStatut(@PathVariable StatutDemande statut) {
        try {
            List<DemandeService> demandes = demandeServiceService.getDemandesParStatut(statut);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/recentes/{jours}")
    public ResponseEntity<List<DemandeService>> getDemandesRecentes(@PathVariable int jours) {
        try {
            List<DemandeService> demandes = demandeServiceService.getDemandesRecentes(jours);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/disponibles-prestataire")
    public ResponseEntity<?> getDemandesDisponiblesPrestataire(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!(utilisateur instanceof com.ecodeli.ecodeli_backend.models.Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent consulter les demandes disponibles"));
            }

            com.ecodeli.ecodeli_backend.models.Prestataire prestataire = (com.ecodeli.ecodeli_backend.models.Prestataire) utilisateur;
            List<DemandeService> demandes = demandeServiceService.getDemandesCompatiblesPrestataire(prestataire);
            return ResponseEntity.ok(demandes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des demandes disponibles"));
        }
    }

    @GetMapping("/compatibles/{categorieService}")
    public ResponseEntity<List<DemandeService>> getDemandesCompatibles(@PathVariable ServiceType categorieService) {
        try {
            List<DemandeService> demandes = demandeServiceService.getDemandesParCategorieDisponibles(categorieService);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Long>> getStatistiques() {
        try {
            Map<String, Long> stats = Map.of(
                "total", demandeServiceService.compterDemandesParStatut(null),
                "publiees", demandeServiceService.compterDemandesParStatut(StatutDemande.PUBLIEE),
                "candidatures_recues", demandeServiceService.compterDemandesParStatut(StatutDemande.CANDIDATURES_RECUES),
                "en_cours", demandeServiceService.compterDemandesParStatut(StatutDemande.EN_COURS),
                "terminees", demandeServiceService.compterDemandesParStatut(StatutDemande.TERMINEE)
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
