package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.CandidatureRepository;
import com.ecodeli.ecodeli_backend.repositories.DemandeServiceRepository;
import com.ecodeli.ecodeli_backend.repositories.PrestataireCategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    @Autowired
    private PrestataireCategorieRepository prestataireCategorieRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Récupérer toutes les candidatures pour une demande de service avec profils prestataires
     */
    public List<Map<String, Object>> getCandidaturesWithPrestatairesByDemande(Long demandeId) {
        List<Candidature> candidatures = candidatureRepository.findByDemandeServiceIdDemande(demandeId.intValue());
        
        return candidatures.stream().map(candidature -> {
            Map<String, Object> candidatureWithPrestataire = new HashMap<>();
            
            candidatureWithPrestataire.put("idCandidature", candidature.getIdCandidature());
            candidatureWithPrestataire.put("prixPropose", candidature.getPrixPropose());
            candidatureWithPrestataire.put("messagePrestataire", candidature.getMessagePrestataire());
            candidatureWithPrestataire.put("statut", candidature.getStatut().name());
            candidatureWithPrestataire.put("dateCandidature", candidature.getDateCandidature());
            candidatureWithPrestataire.put("delaiPropose", candidature.getDelaiPropose());
            
            Prestataire prestataire = candidature.getPrestataire();
            Map<String, Object> prestataireInfo = new HashMap<>();
            prestataireInfo.put("idUtilisateur", prestataire.getIdUtilisateur());
            prestataireInfo.put("nom", prestataire.getNom());
            prestataireInfo.put("prenom", prestataire.getPrenom());
            prestataireInfo.put("email", prestataire.getEmail());
            prestataireInfo.put("telephone", prestataire.getTelephone());
            prestataireInfo.put("adresse", prestataire.getAdresse());
            
            Map<String, Object> statistiques = getStatistiquesPrestataire(prestataire);
            prestataireInfo.put("statistiques", statistiques);
            
            List<Map<String, Object>> categoriesValidees = getCategoriesValideesAvecTarifs(prestataire);
            prestataireInfo.put("categoriesValidees", categoriesValidees);
            
            candidatureWithPrestataire.put("prestataire", prestataireInfo);
            
            return candidatureWithPrestataire;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> accepterCandidature(Long candidatureId, String commentaireClient) {
        try {
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (!candidatureOpt.isPresent()) {
                throw new RuntimeException("Candidature non trouvée avec l'ID: " + candidatureId);
            }
            
            Candidature candidature = candidatureOpt.get();
            
            if (!candidature.peutEtreAcceptee()) {
                throw new RuntimeException("Cette candidature ne peut pas être acceptée (statut: " + candidature.getStatut() + ")");
            }
            
            candidature.accepter(commentaireClient);
            candidatureRepository.save(candidature);
            
            DemandeService demande = candidature.getDemandeService();
            
            demande.setStatut(DemandeService.StatutDemande.PRESTATAIRE_SELECTIONNE);
            demandeServiceRepository.save(demande);
            
            List<Candidature> autresCandidatures = candidatureRepository.findByDemandeServiceIdDemande(demande.getIdDemande().intValue())
                .stream()
                .filter(c -> !c.getIdCandidature().equals(candidatureId) && c.getStatut() == Candidature.StatutCandidature.EN_ATTENTE)
                .collect(Collectors.toList());
            
            for (Candidature autreCandidature : autresCandidatures) {
                autreCandidature.refuser("Candidature non retenue - Un autre prestataire a été sélectionné");
                candidatureRepository.save(autreCandidature);
                
                try {
                    envoyerEmailRefusCandidature(autreCandidature);
                } catch (Exception e) {
                    System.err.println("Erreur envoi email refus: " + e.getMessage());
                }
            }
            
            try {
                envoyerEmailAcceptationCandidature(candidature);
            } catch (Exception e) {
                System.err.println("Erreur envoi email acceptation: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature acceptée avec succès");
            response.put("candidatureId", candidatureId);
            response.put("prestataireNom", candidature.getPrestataire().getPrenom() + " " + candidature.getPrestataire().getNom());
            response.put("nouveauStatutDemande", demande.getStatut().name());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de l'acceptation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public Map<String, Object> refuserCandidature(Long candidatureId, String commentaireClient) {
        try {
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (!candidatureOpt.isPresent()) {
                throw new RuntimeException("Candidature non trouvée avec l'ID: " + candidatureId);
            }
            
            Candidature candidature = candidatureOpt.get();
            
            if (!candidature.peutEtreRefusee()) {
                throw new RuntimeException("Cette candidature ne peut pas être refusée (statut: " + candidature.getStatut() + ")");
            }
            
            candidature.refuser(commentaireClient);
            candidatureRepository.save(candidature);
            
            try {
                envoyerEmailRefusCandidature(candidature);
            } catch (Exception e) {
                System.err.println("Erreur envoi email refus: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature refusée");
            response.put("candidatureId", candidatureId);
            
            return response;
            
        } catch (Exception e) {
            System.err.println("ERREUR lors du refus: " + e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> getStatistiquesPrestataire(Prestataire prestataire) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Candidature> candidaturesPrestataire = candidatureRepository.findByPrestataireIdUtilisateur(prestataire.getIdUtilisateur());
        
        long totalCandidatures = candidaturesPrestataire.size();
        long candidaturesAcceptees = candidaturesPrestataire.stream()
            .filter(c -> c.getStatut() == Candidature.StatutCandidature.ACCEPTEE)
            .count();
        
        double tauxAcceptation = totalCandidatures > 0 ? (candidaturesAcceptees * 100.0 / totalCandidatures) : 0;
        
        stats.put("totalCandidatures", totalCandidatures);
        stats.put("candidaturesAcceptees", candidaturesAcceptees);
        stats.put("tauxAcceptation", Math.round(tauxAcceptation * 100.0) / 100.0);
        stats.put("missionsRealisees", candidaturesAcceptees);
        
        stats.put("noteMoyenne", 4.2); // Valeur fictive pour l'instant
        stats.put("nombreEvaluations", candidaturesAcceptees > 0 ? candidaturesAcceptees : 0);
        
        return stats;
    }

    private List<Map<String, Object>> getCategoriesValideesAvecTarifs(Prestataire prestataire) {
        List<Map<String, Object>> categories = new ArrayList<>();
        
        List<PrestataireCategorie> categoriesPrestataire = prestataireCategorieRepository.findByPrestataireIdUtilisateur(prestataire.getIdUtilisateur());
        
        for (PrestataireCategorie pc : categoriesPrestataire) {
            if (pc.getStatutValidation() == PrestataireCategorie.StatutValidation.VALIDE) {
                Map<String, Object> categorieInfo = new HashMap<>();
                categorieInfo.put("categorieService", pc.getCategorieService().name());
                categorieInfo.put("statut", pc.getStatutValidation().name());
                categorieInfo.put("tarifHoraire", pc.getTarifHoraire());
                categorieInfo.put("dateValidation", pc.getDateValidation());
                categories.add(categorieInfo);
            }
        }
        
        return categories;
    }

    private void envoyerEmailAcceptationCandidature(Candidature candidature) {
        try {
            if (candidature.getPrestataire() != null && candidature.getPrestataire().getEmail() != null) {
                String sujet = "Votre candidature a été acceptée - EcoDeli";
                String contenu = String.format(
                    "<h2>Félicitations ! Votre candidature a été acceptée</h2>" +
                    "<p>Bonjour %s,</p>" +
                    "<p>Excellente nouvelle ! Votre candidature pour la demande <strong>\"%s\"</strong> a été acceptée par le client.</p>" +
                    "<p><strong>Détails de la mission :</strong></p>" +
                    "<ul>" +
                    "<li>Prix convenu : <strong>%s €</strong></li>" +
                    "<li>Délai proposé : <strong>%s jours</strong></li>" +
                    "<li>Date souhaitée : <strong>%s</strong></li>" +
                    "</ul>" +
                    "<p>Vous pouvez maintenant commencer la mission. N'hésitez pas à contacter le client si vous avez des questions.</p>" +
                    "<p>Cordialement,<br>L'équipe EcoDeli</p>",
                    candidature.getPrestataire().getPrenom(),
                    candidature.getDemandeService().getTitre(),
                    candidature.getPrixPropose(),
                    candidature.getDelaiPropose() != null ? candidature.getDelaiPropose() : "Non spécifié",
                    candidature.getDemandeService().getDateSouhaitee() != null ?
                        candidature.getDemandeService().getDateSouhaitee().toLocalDate().toString() : "Non spécifiée"
                );
                
                emailService.sendHtmlEmail(candidature.getPrestataire().getEmail(), sujet, contenu);
            }
        } catch (Exception e) {
            System.err.println("Erreur envoi email acceptation candidature: " + e.getMessage());
        }
    }

    private void envoyerEmailRefusCandidature(Candidature candidature) {
        try {
            if (candidature.getPrestataire() != null && candidature.getPrestataire().getEmail() != null) {
                String sujet = "Candidature non retenue - EcoDeli";
                String contenu = String.format(
                    "<h2>Candidature non retenue</h2>" +
                    "<p>Bonjour %s,</p>" +
                    "<p>Nous vous remercions pour votre candidature concernant la demande <strong>\"%s\"</strong>.</p>" +
                    "<p>Malheureusement, le client a choisi un autre prestataire pour cette mission.</p>" +
                    "<p>Ne vous découragez pas ! De nouvelles opportunités sont publiées régulièrement sur notre plateforme.</p>" +
                    "<p>Continuez à consulter les demandes de services disponibles dans votre espace prestataire.</p>" +
                    "<p>Cordialement,<br>L'équipe EcoDeli</p>",
                    candidature.getPrestataire().getPrenom(),
                    candidature.getDemandeService().getTitre()
                );
                
                emailService.sendHtmlEmail(candidature.getPrestataire().getEmail(), sujet, contenu);
            }
        } catch (Exception e) {
            System.err.println("Erreur envoi email refus candidature: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> creerCandidaturePrestataire(Map<String, Object> candidatureData) {
        try {
            Long demandeId = Long.valueOf(candidatureData.get("demandeId").toString());
            Integer prestataireId = Integer.valueOf(candidatureData.get("prestataireId").toString());
            String messagePrestataire = (String) candidatureData.get("messagePrestataire");
            Integer delaiPropose = candidatureData.get("delaiPropose") != null ?
                Integer.valueOf(candidatureData.get("delaiPropose").toString()) : null;
            Double prixPropose = candidatureData.get("prixPropose") != null ?
                Double.valueOf(candidatureData.get("prixPropose").toString()) : 25.0;
            
            Optional<DemandeService> demandeOpt = demandeServiceRepository.findById(demandeId);
            if (!demandeOpt.isPresent()) {
                throw new RuntimeException("Demande de service non trouvée avec l'ID: " + demandeId);
            }
            
            DemandeService demande = demandeOpt.get();
            
            Boolean existe = candidatureRepository.existsByPrestataireAndDemande(prestataireId, demandeId);
            if (existe) {
                throw new RuntimeException("Vous avez déjà candidaté pour cette demande");
            }
            
            Candidature candidature = new Candidature();
            candidature.setDemandeService(demande);
            candidature.setPrixPropose(java.math.BigDecimal.valueOf(prixPropose));
            candidature.setMessagePrestataire(messagePrestataire);
            candidature.setDelaiPropose(delaiPropose);
            candidature.setStatut(Candidature.StatutCandidature.EN_ATTENTE);
            
            Prestataire prestataireRef = new Prestataire();
            prestataireRef.setIdUtilisateur(prestataireId);
            candidature.setPrestataire(prestataireRef);
        
            candidature = candidatureRepository.save(candidature);
            
            try {
                envoyerEmailNouvelleCandidature(candidature);
            } catch (Exception e) {
                System.err.println("Erreur envoi email: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature envoyée avec succès");
            response.put("candidatureId", candidature.getIdCandidature());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de la création candidature: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void envoyerEmailNouvelleCandidature(Candidature candidature) {
        try {
            if (candidature.getDemandeService() != null &&
                candidature.getDemandeService().getClient() != null &&
                candidature.getDemandeService().getClient().getEmail() != null) {
                
                String sujet = "Nouvelle candidature reçue - EcoDeli";
                String contenu = String.format(
                    "<h2>Nouvelle candidature reçue !</h2>" +
                    "<p>Bonjour %s,</p>" +
                    "<p>Excellente nouvelle ! Vous avez reçu une nouvelle candidature pour votre demande <strong>\"%s\"</strong>.</p>" +
                    "<p><strong>Détails de la candidature :</strong></p>" +
                    "<ul>" +
                    "<li>Prix proposé : <strong>%s €</strong></li>" +
                    "<li>Délai proposé : <strong>%s jours</strong></li>" +
                    "</ul>" +
                    "<p>Connectez-vous à votre espace client pour voir tous les détails et répondre à cette candidature.</p>" +
                    "<p>Cordialement,<br>L'équipe EcoDeli</p>",
                    candidature.getDemandeService().getClient().getPrenom(),
                    candidature.getDemandeService().getTitre(),
                    candidature.getPrixPropose(),
                    candidature.getDelaiPropose() != null ? candidature.getDelaiPropose() : "Non spécifié"
                );
                
                emailService.sendHtmlEmail(candidature.getDemandeService().getClient().getEmail(), sujet, contenu);
            }
        } catch (Exception e) {
            System.err.println("Erreur envoi email nouvelle candidature: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getCandidaturesByPrestataire(Integer prestataireId) {
        List<Candidature> candidatures = candidatureRepository.findByPrestataireIdUtilisateur(prestataireId);
        
        return candidatures.stream().map(candidature -> {
            Map<String, Object> candidatureMap = new HashMap<>();
            
            candidatureMap.put("idCandidature", candidature.getIdCandidature());
            candidatureMap.put("prixPropose", candidature.getPrixPropose());
            candidatureMap.put("messagePrestataire", candidature.getMessagePrestataire());
            candidatureMap.put("statut", candidature.getStatut().name());
            candidatureMap.put("dateCandidature", candidature.getDateCandidature());
            candidatureMap.put("dateCreation", candidature.getDateCandidature());
            candidatureMap.put("delaiPropose", candidature.getDelaiPropose());
            
            if (candidature.getDemandeService() != null) {
                Map<String, Object> demandeMap = new HashMap<>();
                demandeMap.put("idDemande", candidature.getDemandeService().getIdDemande());
                demandeMap.put("titre", candidature.getDemandeService().getTitre());
                demandeMap.put("description", candidature.getDemandeService().getDescription());
                demandeMap.put("categorieService", candidature.getDemandeService().getCategorieService().name());
                demandeMap.put("dateSouhaitee", candidature.getDemandeService().getDateSouhaitee());
                demandeMap.put("adresseDepart", candidature.getDemandeService().getAdresseDepart());
                demandeMap.put("creneauHoraire", candidature.getDemandeService().getCreneauHoraire());
                
                if (candidature.getDemandeService().getClient() != null) {
                    Map<String, Object> clientMap = new HashMap<>();
                    clientMap.put("idUtilisateur", candidature.getDemandeService().getClient().getIdUtilisateur());
                    clientMap.put("prenom", candidature.getDemandeService().getClient().getPrenom());
                    clientMap.put("nom", candidature.getDemandeService().getClient().getNom());
                    clientMap.put("email", candidature.getDemandeService().getClient().getEmail());
                    demandeMap.put("client", clientMap);
                }
                
                candidatureMap.put("demandeService", demandeMap);
            }
            
            return candidatureMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getStatistiquesCandidaturesDemande(Long demandeId) {
        List<Candidature> candidatures = candidatureRepository.findByDemandeServiceIdDemande(demandeId.intValue());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", candidatures.size());
        stats.put("enAttente", candidatures.stream().filter(c -> c.getStatut() == Candidature.StatutCandidature.EN_ATTENTE).count());
        stats.put("acceptees", candidatures.stream().filter(c -> c.getStatut() == Candidature.StatutCandidature.ACCEPTEE).count());
        stats.put("refusees", candidatures.stream().filter(c -> c.getStatut() == Candidature.StatutCandidature.REFUSEE).count());
        
        return stats;
    }
}