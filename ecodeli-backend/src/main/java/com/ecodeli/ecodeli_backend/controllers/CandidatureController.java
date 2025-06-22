package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Candidature;
import com.ecodeli.ecodeli_backend.models.Candidature.StatutCandidature;
import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.services.CandidatureService;
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
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "*")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private DemandeServiceService demandeServiceService;

    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<?> creerCandidature(@Valid @RequestBody Candidature candidature, Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent créer des candidatures"));
            }

            Prestataire prestataire = (Prestataire) utilisateur;
            candidature.setPrestataire(prestataire);

            if (candidature.getDemandeService() == null || candidature.getDemandeService().getIdDemande() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La demande de service est obligatoire"));
            }

            DemandeService demande = demandeServiceService.getDemandeById(candidature.getDemandeService().getIdDemande());
            candidature.setDemandeService(demande);

            Candidature nouvelleCandidature = candidatureService.creerCandidature(candidature);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleCandidature);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création de la candidature"));
        }
    }

    @GetMapping("/mes-candidatures")
    public ResponseEntity<?> getMesCandidatures(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent consulter leurs candidatures"));
            }
            Prestataire prestataire = (Prestataire) utilisateur;
            List<Candidature> candidatures = candidatureService.getCandidaturesActivesParPrestataire(prestataire);
            return ResponseEntity.ok(candidatures);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des candidatures"));
        }
    }

    @GetMapping("/mes-candidatures/toutes")
    public ResponseEntity<?> getToutesMesCandidatures(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent consulter leurs candidatures"));
            }
            Prestataire prestataire = (Prestataire) utilisateur;
            List<Candidature> candidatures = candidatureService.getCandidaturesParPrestataire(prestataire);
            return ResponseEntity.ok(candidatures);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des candidatures"));
        }
    }

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<?> getCandidaturesParDemande(@PathVariable Long demandeId, Authentication authentication) {
        try {
            DemandeService demande = demandeServiceService.getDemandeById(demandeId);
            String email = authentication.getName();

            if (!demande.getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez consulter que les candidatures de vos propres demandes"));
            }

            List<Candidature> candidatures = candidatureService.getCandidaturesParDemande(demande);
            return ResponseEntity.ok(candidatures);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des candidatures"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidatureById(@PathVariable Long id) {
        try {
            Candidature candidature = candidatureService.getCandidatureById(id);
            return ResponseEntity.ok(candidature);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération de la candidature"));
        }
    }

    @PostMapping("/{id}/accepter")
    public ResponseEntity<?> accepterCandidature(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String, String> body,
                                               Authentication authentication) {
        try {
            Candidature candidature = candidatureService.getCandidatureById(id);
            String email = authentication.getName();

            if (!candidature.getDemandeService().getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez accepter que les candidatures de vos propres demandes"));
            }

            String commentaire = (body != null) ? body.get("commentaire") : null;
            Candidature candidatureAcceptee = candidatureService.accepterCandidature(id, commentaire);
            return ResponseEntity.ok(candidatureAcceptee);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de l'acceptation de la candidature"));
        }
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<?> refuserCandidature(@PathVariable Long id,
                                              @RequestBody(required = false) Map<String, String> body,
                                              Authentication authentication) {
        try {
            Candidature candidature = candidatureService.getCandidatureById(id);
            String email = authentication.getName();
            if (!candidature.getDemandeService().getClient().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez refuser que les candidatures de vos propres demandes"));
            }

            String commentaire = (body != null) ? body.get("commentaire") : null;
            Candidature candidatureRefusee = candidatureService.refuserCandidature(id, commentaire);
            return ResponseEntity.ok(candidatureRefusee);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors du refus de la candidature"));
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Candidature>> getCandidaturesParStatut(@PathVariable StatutCandidature statut) {
        try {
            List<Candidature> candidatures = candidatureService.getCandidaturesParStatut(statut);
            return ResponseEntity.ok(candidatures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/mes-candidatures/statut/{statut}")
    public ResponseEntity<?> getMesCandidaturesParStatut(@PathVariable StatutCandidature statut,
                                                        Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent consulter leurs candidatures"));
            }

            Prestataire prestataire = (Prestataire) utilisateur;
            List<Candidature> candidatures = candidatureService.getCandidaturesPrestataireParStatut(prestataire, statut);
            return ResponseEntity.ok(candidatures);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des candidatures"));
        }
    }

    @GetMapping("/prestataire/{prestataireId}/peut-candidater/{demandeId}")
    public ResponseEntity<?> peutCandidater(@PathVariable Long prestataireId, @PathVariable Long demandeId) {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateurById(prestataireId.intValue())
                .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "L'utilisateur n'est pas un prestataire"));
            }

            Prestataire prestataire = (Prestataire) utilisateur;
            DemandeService demande = demandeServiceService.getDemandeById(demandeId);
            boolean peutCandidater = candidatureService.peutCandidater(demande, prestataire);
            return ResponseEntity.ok(Map.of("peutCandidater", peutCandidater));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la vérification"));
        }
    }

    @GetMapping("/mes-candidatures/statistiques")
    public ResponseEntity<?> getStatistiquesPrestataire(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            if (!(utilisateur instanceof Prestataire)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Seuls les prestataires peuvent consulter leurs statistiques"));
            }

            Prestataire prestataire = (Prestataire) utilisateur;
            Long total = candidatureService.compterCandidaturesParPrestataire(prestataire);
            Long enAttente = (long) candidatureService.getCandidaturesPrestataireParStatut(prestataire, StatutCandidature.EN_ATTENTE).size();
            Long acceptees = (long) candidatureService.getCandidaturesPrestataireParStatut(prestataire, StatutCandidature.ACCEPTEE).size();
            Long refusees = (long) candidatureService.getCandidaturesPrestataireParStatut(prestataire, StatutCandidature.REFUSEE).size();
            Map<String, Long> stats = Map.of(
                "total", total,
                "en_attente", enAttente,
                "acceptees", acceptees,
                "refusees", refusees
            );
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des statistiques"));
        }
    }
}
