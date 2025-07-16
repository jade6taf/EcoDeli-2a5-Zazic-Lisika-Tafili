package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.ContratCommercant;
import com.ecodeli.ecodeli_backend.services.ContratService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/commercants/contrats")
@CrossOrigin(origins = "*")
public class ContratCommercantController {

    @Autowired
    private ContratService contratService;

    @PostMapping("/demander")
    public ResponseEntity<?> demanderContrat(@RequestBody Map<String, Object> requestData) {
        try {
            Integer idCommercant = (Integer) requestData.get("idCommercant");
            if (idCommercant == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ID commerçant requis"
                ));
            }

            ContratCommercant contrat = contratService.demanderContrat(idCommercant);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Demande de contrat envoyée avec succès");
            response.put("contrat", formatContratResponse(contrat));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la demande de contrat: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{idCommercant}")
    public ResponseEntity<?> getContratCommercant(@PathVariable Integer idCommercant) {
        try {
            ContratCommercant contrat = contratService.getContratByCommercant(idCommercant);
            
            if (contrat == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "contrat", null,
                    "message", "Aucun contrat trouvé pour ce commerçant"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "contrat", formatContratResponse(contrat)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la récupération du contrat: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{idContrat}/signer")
    public ResponseEntity<?> signerContrat(@PathVariable Integer idContrat, @RequestBody Map<String, Object> requestData) {
        try {
            Integer idCommercant = (Integer) requestData.get("idCommercant");
            Boolean acceptation = (Boolean) requestData.get("acceptation");
            
            if (idCommercant == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ID commerçant requis"
                ));
            }
            
            if (acceptation == null || !acceptation) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Vous devez accepter le contrat pour le signer"
                ));
            }

            ContratCommercant contrat = contratService.signerContrat(idContrat, idCommercant);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contrat signé avec succès",
                "contrat", formatContratResponse(contrat)
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la signature du contrat: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{idCommercant}/statut")
    public ResponseEntity<?> getStatutContrat(@PathVariable Integer idCommercant) {
        try {
            ContratCommercant contrat = contratService.getContratByCommercant(idCommercant);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            if (contrat == null) {
                response.put("statut", "AUCUN_CONTRAT");
                response.put("libelle", "Aucun contrat");
                response.put("peutDemanderContrat", true);
            } else {
                response.put("statut", contrat.getStatut().name());
                response.put("libelle", contrat.getStatut().getLibelle());
                response.put("peutDemanderContrat", false);
                response.put("contrat", formatContratResponse(contrat));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la vérification du statut: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> formatContratResponse(ContratCommercant contrat) {
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
        response.put("commercant", commercantInfo);
        
        return response;
    }
}