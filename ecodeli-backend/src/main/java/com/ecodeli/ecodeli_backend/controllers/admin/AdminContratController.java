package com.ecodeli.ecodeli_backend.controllers.admin;

import com.ecodeli.ecodeli_backend.models.ContratCommercant;
import com.ecodeli.ecodeli_backend.models.TemplateContrat;
import com.ecodeli.ecodeli_backend.models.ContratCommercant.StatutContrat;
import com.ecodeli.ecodeli_backend.services.ContratService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/contrats")
@CrossOrigin(origins = "*")
public class AdminContratController {

    @Autowired
    private ContratService contratService;

    @GetMapping
    public ResponseEntity<?> getAllContrats(@RequestParam(required = false) String statut) {
        try {
            List<ContratCommercant> contrats;

            if (statut != null && !statut.isEmpty()) {
                try {
                    StatutContrat statutEnum = StatutContrat.valueOf(statut.toUpperCase());
                    contrats = contratService.getContratsByStatut(statutEnum);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Statut invalide: " + statut
                    ));
                }
            } else {
                contrats = contratService.getAllContrats();
            }

            List<Map<String, Object>> contratsFormatted = contrats.stream()
                .map(this::formatContratForAdmin)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "contrats", contratsFormatted
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la récupération des contrats: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{idContrat}")
    public ResponseEntity<?> getContratById(@PathVariable Integer idContrat) {
        try {
            ContratCommercant contrat = contratService.getContratById(idContrat);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "contrat", formatContratForAdmin(contrat)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "Contrat non trouvé"
            ));
        }
    }

    @PostMapping("/{idContrat}/creer")
    public ResponseEntity<?> creerContrat(
            @PathVariable Integer idContrat,
            @RequestBody Map<String, Object> requestData) {
        try {
            String contenuContrat = (String) requestData.get("contenuContrat");
            Integer idAdmin = (Integer) requestData.get("idAdmin");
            String commentaire = (String) requestData.get("commentaire");

            if (contenuContrat == null || contenuContrat.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Le contenu du contrat est obligatoire"
                ));
            }

            if (idAdmin == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ID admin requis"
                ));
            }

            ContratCommercant contrat = contratService.creerContrat(idContrat, contenuContrat, idAdmin, commentaire);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contrat créé avec succès",
                "contrat", formatContratForAdmin(contrat)
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la création du contrat: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{idContrat}/statut")
    public ResponseEntity<?> mettreAJourStatut(
            @PathVariable Integer idContrat,
            @RequestBody Map<String, Object> requestData) {
        try {
            String statutStr = (String) requestData.get("statut");
            String commentaire = (String) requestData.get("commentaire");

            if (statutStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Statut requis"
                ));
            }

            StatutContrat nouveauStatut;
            try {
                nouveauStatut = StatutContrat.valueOf(statutStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Statut invalide: " + statutStr
                ));
            }

            ContratCommercant contrat = contratService.mettreAJourStatut(idContrat, nouveauStatut, commentaire);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Statut mis à jour avec succès",
                "contrat", formatContratForAdmin(contrat)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la mise à jour du statut: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{idContrat}")
    public ResponseEntity<?> supprimerContrat(@PathVariable Integer idContrat) {
        try {
            contratService.supprimerContrat(idContrat);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contrat supprimé avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "Contrat non trouvé"
            ));
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getAllTemplates(@RequestParam(required = false, defaultValue = "false") boolean actifsSeuls) {
        try {
            List<TemplateContrat> templates;
            
            if (actifsSeuls) {
                templates = contratService.getTemplatesActifs();
            } else {
                templates = contratService.getAllTemplates();
            }
            
            List<Map<String, Object>> templatesFormatted = templates.stream()
                .map(this::formatTemplateResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "templates", templatesFormatted
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la récupération des templates: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/templates/{idTemplate}")
    public ResponseEntity<?> getTemplateById(@PathVariable Integer idTemplate) {
        try {
            TemplateContrat template = contratService.getTemplateById(idTemplate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "template", formatTemplateResponse(template)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "Template non trouvé"
            ));
        }
    }

    @PostMapping("/templates")
    public ResponseEntity<?> creerTemplate(@RequestBody Map<String, Object> requestData) {
        try {
            String nomTemplate = (String) requestData.get("nomTemplate");
            String contenuTemplate = (String) requestData.get("contenuTemplate");
            String description = (String) requestData.get("description");
            Integer idAdmin = (Integer) requestData.get("idAdmin");

            if (nomTemplate == null || nomTemplate.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Le nom du template est obligatoire"
                ));
            }

            if (contenuTemplate == null || contenuTemplate.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Le contenu du template est obligatoire"
                ));
            }

            if (idAdmin == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ID admin requis"
                ));
            }

            TemplateContrat template = contratService.creerTemplate(nomTemplate, contenuTemplate, description, idAdmin);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Template créé avec succès",
                "template", formatTemplateResponse(template)
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la création du template: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/templates/{idTemplate}")
    public ResponseEntity<?> mettreAJourTemplate(
            @PathVariable Integer idTemplate,
            @RequestBody Map<String, Object> requestData) {
        try {
            TemplateContrat template = contratService.mettreAJourTemplate(idTemplate, requestData);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Template mis à jour avec succès",
                "template", formatTemplateResponse(template)
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la mise à jour du template: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/templates/{idTemplate}")
    public ResponseEntity<?> supprimerTemplate(@PathVariable Integer idTemplate) {
        try {
            contratService.supprimerTemplate(idTemplate);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Template supprimé avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "Template non trouvé"
            ));
        }
    }

    @GetMapping("/statistiques")
    public ResponseEntity<?> getStatistiques() {
        try {
            Map<String, Long> stats = contratService.getStatistiquesContrats();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistiques", stats
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la récupération des statistiques: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> formatContratForAdmin(ContratCommercant contrat) {
        Map<String, Object> response = new HashMap<>();
        response.put("idContrat", contrat.getIdContrat());
        response.put("statut", contrat.getStatut().name());
        response.put("statutLibelle", contrat.getStatut().getLibelle());
        response.put("contenuContrat", contrat.getContenuContrat());
        response.put("dateDemande", contrat.getDateDemande());
        response.put("dateCreationContrat", contrat.getDateCreationContrat());
        response.put("dateSignature", contrat.getDateSignature());
        response.put("commentaireAdmin", contrat.getCommentaireAdmin());
        response.put("signatureCommercant", contrat.getSignatureCommercant());

        Map<String, Object> commercantInfo = new HashMap<>();
        commercantInfo.put("id", contrat.getCommercant().getIdUtilisateur());
        commercantInfo.put("nom", contrat.getCommercant().getNom());
        commercantInfo.put("prenom", contrat.getCommercant().getPrenom());
        commercantInfo.put("nomCommerce", contrat.getCommercant().getNomCommerce());
        commercantInfo.put("email", contrat.getCommercant().getEmail());
        commercantInfo.put("telephone", contrat.getCommercant().getTelephone());
        commercantInfo.put("siret", contrat.getCommercant().getSiret());
        response.put("commercant", commercantInfo);

        if (contrat.getAdminCreateur() != null) {
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", contrat.getAdminCreateur().getIdUtilisateur());
            adminInfo.put("nom", contrat.getAdminCreateur().getNom());
            adminInfo.put("prenom", contrat.getAdminCreateur().getPrenom());
            response.put("adminCreateur", adminInfo);
        }
        
        return response;
    }

    private Map<String, Object> formatTemplateResponse(TemplateContrat template) {
        Map<String, Object> response = new HashMap<>();
        response.put("idTemplate", template.getIdTemplate());
        response.put("nomTemplate", template.getNomTemplate());
        response.put("contenuTemplate", template.getContenuTemplate());
        response.put("description", template.getDescription());
        response.put("actif", template.getActif());
        response.put("dateCreation", template.getDateCreation());
        response.put("dateModification", template.getDateModification());

        if (template.getAdminCreateur() != null) {
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", template.getAdminCreateur().getIdUtilisateur());
            adminInfo.put("nom", template.getAdminCreateur().getNom());
            adminInfo.put("prenom", template.getAdminCreateur().getPrenom());
            response.put("adminCreateur", adminInfo);
        }
        
        return response;
    }

    @PostMapping("/templates/init-default")
    public ResponseEntity<?> initDefaultTemplate() {
        try {
            contratService.initDefaultTemplate();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Template par défaut initialisé avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de l'initialisation du template: " + e.getMessage()
            ));
        }
    }
}