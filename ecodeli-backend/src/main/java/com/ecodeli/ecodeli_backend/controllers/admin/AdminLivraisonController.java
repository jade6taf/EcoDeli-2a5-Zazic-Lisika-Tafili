package com.ecodeli.ecodeli_backend.controllers.admin;

import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.services.LivraisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/livraisons")
@CrossOrigin(origins = "*")
public class AdminLivraisonController {

    @Autowired
    private LivraisonService livraisonService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLivraisons(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type) {
        List<Livraison> livraisons = livraisonService.getAllLivraisons();

        if (statut != null && !statut.isEmpty()) {
            try {
                Livraison.StatutLivraison statutEnum = Livraison.StatutLivraison.valueOf(statut.toUpperCase());
                livraisons = livraisons.stream()
                    .filter(l -> l.getStatut() == statutEnum)
                    .toList();
            } catch (IllegalArgumentException e) {
                // ignore le filtre
            }
        }

        if (type != null && !type.isEmpty()) {
            try {
                Livraison.TypeLivraison typeEnum = Livraison.TypeLivraison.valueOf(type.toUpperCase());
                livraisons = livraisons.stream()
                    .filter(l -> l.getTypeLivraison() == typeEnum)
                    .toList();
            } catch (IllegalArgumentException e) {
                // ignore le filtre
            }
        }

        List<Map<String, Object>> enrichedLivraisons = livraisons.stream()
            .map(this::enrichLivraisonData)
            .toList();
        return ResponseEntity.ok(enrichedLivraisons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLivraisonById(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.getLivraisonById(id);
            Map<String, Object> enrichedData = enrichLivraisonData(livraison);
            return ResponseEntity.ok(enrichedData);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Livraison non trouvée");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, Object>> updateStatut(
            @PathVariable Integer id,
            @RequestBody Map<String, String> data) {

        try {
            String nouveauStatut = data.get("statut");
            Livraison.StatutLivraison statutEnum = Livraison.StatutLivraison.valueOf(nouveauStatut.toUpperCase());

            Livraison livraison = livraisonService.updateStatutLivraison(id, statutEnum);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Statut mis à jour avec succès");
            response.put("livraison", enrichLivraisonData(livraison));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Statut invalide");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<Map<String, Object>> annulerLivraison(@PathVariable Integer id) {
        try {
            Livraison livraison = livraisonService.annulerLivraison(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Livraison annulée avec succès");
            response.put("livraison", enrichLivraisonData(livraison));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLivraisonsStats() {
        List<Livraison> livraisons = livraisonService.getAllLivraisons();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalLivraisons", livraisons.size());

        Map<String, Long> parStatut = new HashMap<>();
        for (Livraison.StatutLivraison statut : Livraison.StatutLivraison.values()) {
            long count = livraisons.stream()
                .filter(l -> l.getStatut() == statut)
                .count();
            parStatut.put(statut.name(), count);
        }
        stats.put("parStatut", parStatut);

        Map<String, Long> parType = new HashMap<>();
        for (Livraison.TypeLivraison type : Livraison.TypeLivraison.values()) {
            long count = livraisons.stream()
                .filter(l -> l.getTypeLivraison() == type)
                .count();
            parType.put(type.name(), count);
        }
        stats.put("parType", parType);

        Integer revenusTotal = livraisons.stream()
            .filter(l -> l.getStatut() == Livraison.StatutLivraison.TERMINEE)
            .mapToInt(l -> l.getPrix() != null ? l.getPrix() : 0)
            .sum();
        stats.put("revenusTotal", revenusTotal);

        return ResponseEntity.ok(stats);
    }

    private Map<String, Object> enrichLivraisonData(Livraison livraison) {
        Map<String, Object> data = new HashMap<>();

        data.put("idLivraison", livraison.getIdLivraison());
        data.put("statut", livraison.getStatut().name());
        data.put("statutLabel", getStatutLabel(livraison.getStatut()));
        data.put("typeLivraison", livraison.getTypeLivraison().name());
        data.put("typeLivraisonLabel", getTypeLabel(livraison.getTypeLivraison()));
        data.put("prix", livraison.getPrix());
        data.put("adresseEnvoi", livraison.getAdresseEnvoi());
        data.put("codePostalEnvoi", livraison.getCodePostalEnvoi());
        data.put("adresseDeLivraison", livraison.getAdresseDeLivraison());
        data.put("codePostalLivraison", livraison.getCodePostalLivraison());
        data.put("dateDebut", livraison.getDateDebut());
        data.put("dateFin", livraison.getDateFin());
        data.put("validation", livraison.getValidation());
        data.put("codeValidation", livraison.getCodeValidation());

        if (livraison.getExpediteur() != null) {
            Map<String, Object> expediteurData = new HashMap<>();
            expediteurData.put("id", livraison.getExpediteur().getIdUtilisateur());
            expediteurData.put("nom", livraison.getExpediteur().getNom());
            expediteurData.put("prenom", livraison.getExpediteur().getPrenom());
            expediteurData.put("email", livraison.getExpediteur().getEmail());
            data.put("expediteur", expediteurData);
        }

        if (livraison.getDestinataire() != null) {
            Map<String, Object> destinataireData = new HashMap<>();
            destinataireData.put("id", livraison.getDestinataire().getIdUtilisateur());
            destinataireData.put("nom", livraison.getDestinataire().getNom());
            destinataireData.put("prenom", livraison.getDestinataire().getPrenom());
            destinataireData.put("email", livraison.getDestinataire().getEmail());
            data.put("destinataire", destinataireData);
        }

        if (livraison.getAnnonce() != null) {
            Map<String, Object> annonceData = new HashMap<>();
            annonceData.put("id", livraison.getAnnonce().getIdAnnonce());
            annonceData.put("titre", livraison.getAnnonce().getTitre());
            annonceData.put("description", livraison.getAnnonce().getDescription());
            annonceData.put("emailDestinataire", livraison.getAnnonce().getEmailDestinataire());
            data.put("annonce", annonceData);
        }

        if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
            data.put("entrepotVille", livraison.getEntrepotVille());
            data.put("dateDepotEntrepot", livraison.getDateDepotEntrepot());
            data.put("dateCollecteEntrepot", livraison.getDateCollecteEntrepot());

            if (livraison.getLivreurSegment1() != null) {
                Map<String, Object> livreur1Data = new HashMap<>();
                livreur1Data.put("id", livraison.getLivreurSegment1().getIdUtilisateur());
                livreur1Data.put("nom", livraison.getLivreurSegment1().getNom());
                livreur1Data.put("prenom", livraison.getLivreurSegment1().getPrenom());
                data.put("livreurSegment1", livreur1Data);
            }

            if (livraison.getLivreurSegment2() != null) {
                Map<String, Object> livreur2Data = new HashMap<>();
                livreur2Data.put("id", livraison.getLivreurSegment2().getIdUtilisateur());
                livreur2Data.put("nom", livraison.getLivreurSegment2().getNom());
                livreur2Data.put("prenom", livraison.getLivreurSegment2().getPrenom());
                data.put("livreurSegment2", livreur2Data);
            }
        }

        return data;
    }

    private String getStatutLabel(Livraison.StatutLivraison statut) {
        return switch (statut) {
            case VALIDEE -> "Validée";
            case EN_COURS -> "En cours";
            case ATTENTE_SEGMENT_2 -> "Attente segment 2";
            case SEGMENT_2_EN_COURS -> "Segment 2 en cours";
            case ARRIVED -> "Arrivé";
            case TERMINEE -> "Terminée";
            case ANNULEE -> "Annulée";
        };
    }

    private String getTypeLabel(Livraison.TypeLivraison type) {
        return switch (type) {
            case DIRECTE -> "Directe";
            case PARTIELLE -> "Partielle";
        };
    }
}
