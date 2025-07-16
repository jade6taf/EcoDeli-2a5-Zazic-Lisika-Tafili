package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PrixCalculService {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private PrestataireCategorieRepository prestataireCategorieRepository;

    public BigDecimal calculerPrixMission(Long candidatureId) {
        try {
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (!candidatureOpt.isPresent()) {
                throw new RuntimeException("Candidature non trouvée: " + candidatureId);
            }
            
            Candidature candidature = candidatureOpt.get();
            Prestataire prestataire = candidature.getPrestataire();
            DemandeService demande = candidature.getDemandeService();
            
            if (prestataire == null || demande == null) {
                throw new RuntimeException("Données candidature incomplètes");
            }
            
            ServiceType categorieService = demande.getCategorieService();
            Optional<PrestataireCategorie> categorieOpt = prestataireCategorieRepository
                .findByPrestataireIdUtilisateurAndCategorieService(prestataire.getIdUtilisateur(), categorieService);
            
            if (!categorieOpt.isPresent()) {
                System.err.println("Aucun tarif trouvé pour prestataire " + prestataire.getIdUtilisateur() +
                                 " catégorie " + categorieService);
                return new BigDecimal("25.00");
            }
            
            PrestataireCategorie categorie = categorieOpt.get();
            BigDecimal tarifHoraire = BigDecimal.valueOf(categorie.getTarifHoraire() != null ? categorie.getTarifHoraire() : 25.0);
            
            BigDecimal dureeHeures = new BigDecimal("1.0"); // 1h par défaut
            
            BigDecimal prixCalcule = tarifHoraire.multiply(dureeHeures)
                .setScale(2, RoundingMode.HALF_UP);
            
            return prixCalcule;
            
        } catch (Exception e) {
            System.err.println("Erreur calcul prix mission: " + e.getMessage());
            e.printStackTrace();
            return new BigDecimal("25.00");
        }
    }

    public Map<String, Object> getDetailsPrix(Long candidatureId) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            BigDecimal prixCalcule = calculerPrixMission(candidatureId);
            
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (candidatureOpt.isPresent()) {
                Candidature candidature = candidatureOpt.get();
                Prestataire prestataire = candidature.getPrestataire();
                DemandeService demande = candidature.getDemandeService();
                
                ServiceType categorieService = demande.getCategorieService();
                Optional<PrestataireCategorie> categorieOpt = prestataireCategorieRepository
                    .findByPrestataireIdUtilisateurAndCategorieService(prestataire.getIdUtilisateur(), categorieService);
                
                if (categorieOpt.isPresent()) {
                    PrestataireCategorie categorie = categorieOpt.get();
                    
                    details.put("prixTotal", prixCalcule);
                    details.put("tarifHoraire", BigDecimal.valueOf(categorie.getTarifHoraire() != null ? categorie.getTarifHoraire() : 25.0));
                    details.put("dureeEstimee", new BigDecimal("1.0"));
                    details.put("categorie", categorieService.toString());
                    details.put("calculMethode", "tarif_x_duree");
                    details.put("prestataireNom", prestataire.getPrenom() + " " + prestataire.getNom());
                } else {
                    details.put("prixTotal", prixCalcule);
                    details.put("tarifHoraire", new BigDecimal("25.00"));
                    details.put("dureeEstimee", new BigDecimal("1.0"));
                    details.put("categorie", categorieService.toString());
                    details.put("calculMethode", "fallback");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur détails prix: " + e.getMessage());
            details.put("prixTotal", new BigDecimal("25.00"));
            details.put("calculMethode", "erreur");
        }
        
        return details;
    }
}