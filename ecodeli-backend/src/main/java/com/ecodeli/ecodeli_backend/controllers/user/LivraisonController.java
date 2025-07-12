package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.services.LivraisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/livraisons")
@CrossOrigin(origins = "*")
public class LivraisonController {

    @Autowired
    private LivraisonService livraisonService;

    @GetMapping("/livreur/{livreurId}")
    public ResponseEntity<List<Map<String, Object>>> getLivraisonsByLivreur(@PathVariable Integer livreurId) {
        List<Livraison> livraisons = livraisonService.getLivraisonsByLivreur(livreurId);

        List<Map<String, Object>> enrichedLivraisons = livraisons.stream()
            .map(livraison -> {
                Map<String, Object> livraisonData = new HashMap<>();
                livraisonData.put("idLivraison", livraison.getIdLivraison());
                livraisonData.put("statut", livraison.getStatut());
                livraisonData.put("typeLivraison", livraison.getTypeLivraison());

                Integer prixAffiche = livraison.getPrix();
                if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
                    prixAffiche = prixAffiche / 2;
                }
                livraisonData.put("prix", prixAffiche);
                livraisonData.put("prixTotal", livraison.getPrix());
                livraisonData.put("adresseEnvoi", livraison.getAdresseEnvoi());
                livraisonData.put("adresseDeLivraison", livraison.getAdresseDeLivraison());
                livraisonData.put("dateDebut", livraison.getDateDebut());
                livraisonData.put("dateFin", livraison.getDateFin());

                if (livraison.getAnnonce() != null) {
                    livraisonData.put("titre", livraison.getAnnonce().getTitre());
                    livraisonData.put("description", livraison.getAnnonce().getDescription());
                    livraisonData.put("emailDestinataire", livraison.getAnnonce().getEmailDestinataire());
                }

                if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
                    livraisonData.put("entrepotVille", livraison.getEntrepotVille());
                    livraisonData.put("dateDepotEntrepot", livraison.getDateDepotEntrepot());
                    livraisonData.put("dateCollecteEntrepot", livraison.getDateCollecteEntrepot());

                    String segment = livraisonService.getSegmentForLivreur(livraison, livreurId);
                    livraisonData.put("segment", segment);

                    boolean canStart = false;
                    String statusMessage = "";

                    if ("SEGMENT_1".equals(segment)) {
                        canStart = livraison.getStatut() == Livraison.StatutLivraison.VALIDEE;
                        statusMessage = canStart ? "Prêt à démarrer" : "En attente";
                    } else if ("SEGMENT_2".equals(segment)) {
                        canStart = livraison.getStatut() == Livraison.StatutLivraison.ATTENTE_SEGMENT_2;
                        statusMessage = canStart ? "Prêt à démarrer" :
                                       (livraison.getStatut() == Livraison.StatutLivraison.VALIDEE ? "En attente du segment 1" : "En cours");
                    }

                    livraisonData.put("canStart", canStart);
                    livraisonData.put("statusMessage", statusMessage);
                }

                return livraisonData;
            })
            .toList();

        return ResponseEntity.ok(enrichedLivraisons);
    }

    @PutMapping("/{livraisonId}/start")
    public ResponseEntity<Map<String, Object>> startLivraison(
            @PathVariable Integer livraisonId,
            @RequestBody Map<String, Object> data) {

        try {
            Integer livreurId = (Integer) data.get("livreurId");
            livraisonService.startLivraison(livraisonId, livreurId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Livraison démarrée avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{livraisonId}/complete")
    public ResponseEntity<Map<String, Object>> completeLivraison(
            @PathVariable Integer livraisonId,
            @RequestBody Map<String, Object> data) {

        try {
            Integer livreurId = (Integer) data.get("livreurId");
            livraisonService.completeLivraison(livraisonId, livreurId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Livraison terminée avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{livraisonId}/validate-otp")
    public ResponseEntity<Map<String, Object>> validateOTP(
            @PathVariable Integer livraisonId,
            @RequestBody Map<String, String> data) {

        try {
            String otp = data.get("otp");
            boolean isValid = livraisonService.validateOTP(livraisonId, otp);

            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                response.put("success", true);
                response.put("message", "Livraison finalisée avec succès !");
            } else {
                response.put("success", false);
                response.put("message", "Code OTP incorrect ou expiré");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{livraisonId}")
    public ResponseEntity<Map<String, Object>> getLivraisonDetails(@PathVariable Integer livraisonId) {
        try {
            Livraison livraison = livraisonService.getLivraisonById(livraisonId);

            Map<String, Object> livraisonData = new HashMap<>();
            livraisonData.put("idLivraison", livraison.getIdLivraison());
            livraisonData.put("statut", livraison.getStatut());
            livraisonData.put("typeLivraison", livraison.getTypeLivraison());
            livraisonData.put("prix", livraison.getPrix());
            livraisonData.put("adresseEnvoi", livraison.getAdresseEnvoi());
            livraisonData.put("adresseDeLivraison", livraison.getAdresseDeLivraison());
            livraisonData.put("dateDebut", livraison.getDateDebut());
            livraisonData.put("dateFin", livraison.getDateFin());
            livraisonData.put("validation", livraison.getValidation());

            if (livraison.getAnnonce() != null) {
                Map<String, Object> annonceData = new HashMap<>();
                annonceData.put("titre", livraison.getAnnonce().getTitre());
                annonceData.put("description", livraison.getAnnonce().getDescription());
                annonceData.put("emailDestinataire", livraison.getAnnonce().getEmailDestinataire());

                if (livraison.getAnnonce().getColis() != null) {
                    Map<String, Object> colisData = new HashMap<>();
                    colisData.put("poids", livraison.getAnnonce().getColis().getPoids());
                    colisData.put("fragile", livraison.getAnnonce().getColis().getFragile());
                    colisData.put("description", livraison.getAnnonce().getColis().getDescription());
                    annonceData.put("colis", colisData);
                }

                livraisonData.put("annonce", annonceData);
            }

            if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
                livraisonData.put("entrepotVille", livraison.getEntrepotVille());
                livraisonData.put("dateDepotEntrepot", livraison.getDateDepotEntrepot());
                livraisonData.put("dateCollecteEntrepot", livraison.getDateCollecteEntrepot());

                if (livraison.getLivreurSegment1() != null) {
                    livraisonData.put("livreur1", livraison.getLivreurSegment1().getPrenom() + " " + livraison.getLivreurSegment1().getNom());
                }
                if (livraison.getLivreurSegment2() != null) {
                    livraisonData.put("livreur2", livraison.getLivreurSegment2().getPrenom() + " " + livraison.getLivreurSegment2().getNom());
                }
            }

            return ResponseEntity.ok(livraisonData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
