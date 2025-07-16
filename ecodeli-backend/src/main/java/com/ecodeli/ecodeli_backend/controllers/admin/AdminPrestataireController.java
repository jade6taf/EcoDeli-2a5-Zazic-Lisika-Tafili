package com.ecodeli.ecodeli_backend.controllers.admin;

import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.PrestataireCategorie;
import com.ecodeli.ecodeli_backend.models.Justificatif;
import com.ecodeli.ecodeli_backend.services.AdminPrestataireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/prestataires")
@CrossOrigin(origins = "*")
public class AdminPrestataireController {

    @Autowired
    private AdminPrestataireService adminPrestataireService;

    /**
     * Récupérer tous les prestataires
     */
    @GetMapping
    public ResponseEntity<List<Prestataire>> getAllPrestataires() {
        List<Prestataire> prestataires = adminPrestataireService.getAllPrestataires();
        return ResponseEntity.ok(prestataires);
    }

    /**
     * Récupérer un prestataire par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prestataire> getPrestataireById(@PathVariable Integer id) {
        try {
            Prestataire prestataire = adminPrestataireService.getPrestataireById(id);
            return ResponseEntity.ok(prestataire);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer les validations de catégories d'un prestataire
     */
    @GetMapping("/{id}/validations")
    public ResponseEntity<List<PrestataireCategorie>> getValidationsPrestataire(@PathVariable Integer id) {
        List<PrestataireCategorie> validations = adminPrestataireService.getValidationsPrestataire(id);
        return ResponseEntity.ok(validations);
    }

    /**
     * Valider ou rejeter une catégorie pour un prestataire
     */
    @PostMapping("/{id}/validation")
    public ResponseEntity<Map<String, Object>> validerCategoriePrestataire(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> validationData) {
        try {
            String categorie = (String) validationData.get("categorie");
            String statut = (String) validationData.get("statut");
            String commentaire = (String) validationData.get("commentaire");

            PrestataireCategorie validation = adminPrestataireService.validerCategoriePrestataire(
                    id, categorie, statut, commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Validation mise à jour avec succès");
            response.put("validation", validation);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Définir le tarif horaire d'un prestataire pour une catégorie
     */
    @PostMapping("/{id}/tarif")
    public ResponseEntity<Map<String, Object>> definirTarifPrestataire(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> tarifData) {
        try {
            String categorie = (String) tarifData.get("categorie");
            Double tarifHoraire = ((Number) tarifData.get("tarifHoraire")).doubleValue();

            PrestataireCategorie tarifDefini = adminPrestataireService.definirTarifPrestataire(
                    id, categorie, tarifHoraire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tarif défini avec succès");
            response.put("tarif", tarifDefini);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Récupérer les justificatifs d'un prestataire
     */
    @GetMapping("/{id}/justificatifs")
    public ResponseEntity<List<Justificatif>> getJustificatifsPrestataire(@PathVariable Integer id) {
        List<Justificatif> justificatifs = adminPrestataireService.getJustificatifsPrestataire(id);
        return ResponseEntity.ok(justificatifs);
    }

    /**
     * Valider ou rejeter un justificatif
     */
    @PutMapping("/justificatifs/{justificatifId}/validation")
    public ResponseEntity<Map<String, Object>> validerJustificatif(
            @PathVariable Integer justificatifId,
            @RequestBody Map<String, Object> validationData) {
        try {
            Boolean statut = (Boolean) validationData.get("statut");
            String commentaire = (String) validationData.get("commentaire");

            Justificatif justificatif = adminPrestataireService.validerJustificatif(
                    justificatifId, statut, commentaire);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Justificatif validé avec succès");
            response.put("justificatif", justificatif);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Rechercher des prestataires avec filtres
     */
    @PostMapping("/recherche")
    public ResponseEntity<List<Prestataire>> rechercherPrestataires(@RequestBody Map<String, Object> filtres) {
        List<Prestataire> prestataires = adminPrestataireService.rechercherPrestataires(filtres);
        return ResponseEntity.ok(prestataires);
    }

    /**
     * Obtenir les statistiques des prestataires
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistiquesPrestataires() {
        Map<String, Object> stats = adminPrestataireService.getStatistiquesPrestataires();
        return ResponseEntity.ok(stats);
    }

    /**
     * Exporter la liste des prestataires
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exporterPrestataires(@RequestParam(defaultValue = "csv") String format) {
        try {
            byte[] data = adminPrestataireService.exporterPrestataires(format);
            
            String contentType = format.equals("pdf") ? "application/pdf" : "text/csv";
            String fileName = "prestataires_" + java.time.LocalDate.now() + "." + format;
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer les prestataires par catégorie de service
     */
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Prestataire>> getPrestatairesParCategorie(@PathVariable String categorie) {
        List<Prestataire> prestataires = adminPrestataireService.getPrestatairesParCategorie(categorie);
        return ResponseEntity.ok(prestataires);
    }

    /**
     * Récupérer les prestataires validés
     */
    @GetMapping("/valides")
    public ResponseEntity<List<Prestataire>> getPrestatairesValides() {
        List<Prestataire> prestataires = adminPrestataireService.getPrestatairesValides();
        return ResponseEntity.ok(prestataires);
    }

    /**
     * Récupérer les prestataires en attente de validation
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<Prestataire>> getPrestatairesEnAttente() {
        List<Prestataire> prestataires = adminPrestataireService.getPrestatairesEnAttente();
        return ResponseEntity.ok(prestataires);
    }

    /**
     * Activer/désactiver un prestataire
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, Object>> changerStatutPrestataire(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> statutData) {
        try {
            Boolean actif = statutData.get("actif");
            adminPrestataireService.changerStatutPrestataire(id, actif);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Statut du prestataire mis à jour");
            response.put("actif", actif);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}