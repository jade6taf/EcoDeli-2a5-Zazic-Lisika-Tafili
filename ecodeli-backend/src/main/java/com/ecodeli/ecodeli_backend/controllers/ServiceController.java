package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.models.Service.StatutService;
import com.ecodeli.ecodeli_backend.services.ServiceService;
import com.ecodeli.ecodeli_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{idService}/commencer")
    public ResponseEntity<?> commencerService(
            @PathVariable Long idService,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            Service service = serviceService.commencerService(idService, idUtilisateur);
            return ResponseEntity.ok(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne du serveur"));
        }
    }

    @PostMapping("/{idService}/terminer")
    public ResponseEntity<?> terminerService(
            @PathVariable Long idService,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            String notes = requestBody != null ? requestBody.get("notes") : null;
            Service service = serviceService.terminerService(idService, idUtilisateur, notes);
            return ResponseEntity.ok(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne du serveur"));
        }
    }

    @PostMapping("/{idService}/annuler")
    public ResponseEntity<?> annulerService(
            @PathVariable Long idService,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            String raison = requestBody != null ? requestBody.get("raison") : null;
            Service service = serviceService.annulerService(idService, idUtilisateur, raison);
            return ResponseEntity.ok(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne du serveur"));
        }
    }

    @GetMapping("/en-cours")
    public ResponseEntity<?> getServicesEnCours(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            List<Service> services = serviceService.getServicesEnCoursPrestataire(idUtilisateur);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services"));
        }
    }

    @GetMapping("/termines")
    public ResponseEntity<?> getServicesTermines(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            List<Service> services = serviceService.getServicesTerminesPrestataire(idUtilisateur);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services"));
        }
    }

    @GetMapping("/mes-services")
    public ResponseEntity<?> getMesServices(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            List<Service> services = serviceService.getServicesPrestataire(idUtilisateur);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services"));
        }
    }

    @GetMapping("/client/mes-services")
    public ResponseEntity<?> getMesServicesClient(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            List<Service> services = serviceService.getServicesClient(idUtilisateur);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services"));
        }
    }

    @GetMapping("/client/mes-services/{statut}")
    public ResponseEntity<?> getMesServicesClientParStatut(
            @PathVariable String statut,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            StatutService statutEnum = StatutService.valueOf(statut.toUpperCase());
            List<Service> services = serviceService.getServicesClientParStatut(idUtilisateur, statutEnum);

            return ResponseEntity.ok(services);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Statut invalide: " + statut));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services"));
        }
    }

    @GetMapping("/{idService}")
    public ResponseEntity<?> getServiceDetails(@PathVariable Long idService, HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Service service = serviceService.getServiceById(idService);

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            boolean hasAccess = service.getClient().getIdUtilisateur().equals(idUtilisateur) ||
                              service.getPrestataire().getIdUtilisateur().equals(idUtilisateur);

            if (!hasAccess) {
                return ResponseEntity.status(403).body(Map.of("error", "Accès non autorisé"));
            }

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération du service"));
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiquesPrestataire(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            ServiceService.StatistiquesPrestataire stats = serviceService.getStatistiquesPrestataire(idUtilisateur);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des statistiques"));
        }
    }

    @GetMapping("/urgents")
    public ResponseEntity<?> getServicesUrgents(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            List<Service> services = serviceService.getServicesUrgentsPrestataire(idUtilisateur);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la récupération des services urgents"));
        }
    }

    @PutMapping("/{idService}/notes")
    public ResponseEntity<?> mettreAJourNotes(
            @PathVariable Long idService,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            Integer idUtilisateur = jwtUtil.extractUserId(token);
            String notes = requestBody.get("notes");

            if (notes == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Notes manquantes"));
            }

            Service service = serviceService.mettreAJourNotes(idService, idUtilisateur, notes);

            return ResponseEntity.ok(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la mise à jour des notes"));
        }
    }

    @PutMapping("/{idService}/progression")
    public ResponseEntity<?> mettreAJourProgression(
            @PathVariable Long idService,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }
            Integer idUtilisateur = jwtUtil.extractUserId(token);
            Object progressionObj = requestBody.get("progression");
            if (progressionObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Progression manquante"));
            }
            Integer progression;
            if (progressionObj instanceof Integer) {
                progression = (Integer) progressionObj;
            } else if (progressionObj instanceof String) {
                try {
                    progression = Integer.parseInt((String) progressionObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Format de progression invalide"));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Format de progression invalide"));
            }

            String notes = (String) requestBody.get("notes");
            Service service = serviceService.mettreAJourProgression(idService, idUtilisateur, progression, notes);

            return ResponseEntity.ok(service);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la mise à jour de la progression"));
        }
    }

    @PostMapping("/creer-depuis-candidature/{idCandidature}")
    public ResponseEntity<?> creerServiceDepuisCandidature(
            @PathVariable Long idCandidature,
            HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token manquant"));
            }

            return ResponseEntity.ok(Map.of("message", "Service créé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la création du service"));
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
