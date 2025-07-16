package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissionService {

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    public List<Map<String, Object>> getMissionsByPrestataire(Integer prestataireId) {
        List<Candidature> candidaturesAcceptees = candidatureRepository.findAcceptedByPrestataire(prestataireId);
        
        return candidaturesAcceptees.stream().map(candidature -> {
            Map<String, Object> missionMap = new HashMap<>();
            
            missionMap.put("id", candidature.getIdCandidature());
            missionMap.put("candidatureId", candidature.getIdCandidature());
            
            DemandeService demande = candidature.getDemandeService();
            String statutMission = determinerStatutMission(demande.getStatut());
            missionMap.put("statut", statutMission);
            
            missionMap.put("prixAccepte", candidature.getPrixPropose());
            
            missionMap.put("dateAcceptation", candidature.getDateReponse());
            
            if (demande.getStatut() == DemandeService.StatutDemande.EN_COURS) {
                missionMap.put("dateDebut", candidature.getDateReponse());
            }
            
            if (demande.getStatut() == DemandeService.StatutDemande.TERMINEE) {
                missionMap.put("dateTerminaison", demande.getDateModification());
            }
            
            if (demande != null) {
                Map<String, Object> demandeMap = new HashMap<>();
                demandeMap.put("idDemande", demande.getIdDemande());
                demandeMap.put("titre", demande.getTitre());
                demandeMap.put("description", demande.getDescription());
                demandeMap.put("categorieService", demande.getCategorieService().name());
                demandeMap.put("dateSouhaitee", demande.getDateSouhaitee());
                demandeMap.put("adresseDepart", demande.getAdresseDepart());
                demandeMap.put("adresseArrivee", demande.getAdresseArrivee());
                demandeMap.put("creneauHoraire", demande.getCreneauHoraire());
                
                if (demande.getClient() != null) {
                    Map<String, Object> clientMap = new HashMap<>();
                    clientMap.put("idUtilisateur", demande.getClient().getIdUtilisateur());
                    clientMap.put("prenom", demande.getClient().getPrenom());
                    clientMap.put("nom", demande.getClient().getNom());
                    clientMap.put("email", demande.getClient().getEmail());
                    clientMap.put("telephone", demande.getClient().getTelephone());
                    demandeMap.put("client", clientMap);
                }
                
                missionMap.put("demandeService", demandeMap);
            }
            
            return missionMap;
        }).collect(Collectors.toList());
    }

    private String determinerStatutMission(DemandeService.StatutDemande statutDemande) {
        switch (statutDemande) {
            case PRESTATAIRE_SELECTIONNE:
                return "PRESTATAIRE_SELECTIONNE";
            case EN_COURS:
                return "EN_COURS";
            case TERMINEE:
                return "TERMINEE";
            default:
                return "PRESTATAIRE_SELECTIONNE";
        }
    }

    @Transactional
    public Map<String, Object> demarrerMission(Long missionId) {
        Optional<Candidature> candidatureOpt = candidatureRepository.findById(missionId);
        if (!candidatureOpt.isPresent()) {
            throw new RuntimeException("Mission/Candidature non trouvée avec ID: " + missionId);
        }
        
        Candidature candidature = candidatureOpt.get();
        DemandeService demande = candidature.getDemandeService();
        
        if (demande.getStatut() != DemandeService.StatutDemande.PRESTATAIRE_SELECTIONNE) {
            throw new RuntimeException("La mission ne peut pas être démarrée. Statut actuel: " + demande.getStatut());
        }
        
        demande.setStatut(DemandeService.StatutDemande.EN_COURS);
        demande.setDateModification(LocalDateTime.now());
        demandeServiceRepository.save(demande);
        
        try {
            envoyerEmailDemarrageClient(candidature);
        } catch (Exception e) {
            System.err.println("Erreur envoi email démarrage: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Mission démarrée avec succès");
        result.put("missionId", missionId);
        result.put("nouveauStatut", "EN_COURS");
        
        return result;
    }

    @Transactional
    public Map<String, Object> terminerMission(Long missionId, String noteFinale, String photoFinale) {
        Optional<Candidature> candidatureOpt = candidatureRepository.findById(missionId);
        if (!candidatureOpt.isPresent()) {
            throw new RuntimeException("Mission/Candidature non trouvée avec ID: " + missionId);
        }
        
        Candidature candidature = candidatureOpt.get();
        DemandeService demande = candidature.getDemandeService();
        
        if (demande.getStatut() != DemandeService.StatutDemande.EN_COURS) {
            throw new RuntimeException("La mission ne peut pas être terminée. Statut actuel: " + demande.getStatut());
        }
        
        demande.setStatut(DemandeService.StatutDemande.TERMINEE);
        demande.setDateModification(LocalDateTime.now());
        demandeServiceRepository.save(demande);
        
        if (noteFinale != null && !noteFinale.trim().isEmpty()) {
            candidature.setCommentaireClient(noteFinale);
            candidatureRepository.save(candidature);
        }
        
        try {
            envoyerEmailTerminaisonClient(candidature, noteFinale);
        } catch (Exception e) {
            System.err.println("Erreur envoi email terminaison: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Mission terminée avec succès. Le client va être notifié.");
        result.put("missionId", missionId);
        result.put("nouveauStatut", "TERMINEE");
        
        return result;
    }

    public Map<String, Object> getDetailsMission(Long missionId) {
        Optional<Candidature> candidatureOpt = candidatureRepository.findById(missionId);
        if (!candidatureOpt.isPresent()) {
            return null;
        }
        
        Candidature candidature = candidatureOpt.get();
        List<Map<String, Object>> missions = getMissionsByPrestataire(candidature.getPrestataire().getIdUtilisateur());
        
        return missions.stream()
            .filter(mission -> mission.get("id").equals(missionId))
            .findFirst()
            .orElse(null);
    }

    public List<Map<String, Object>> getMissionsByClient(Integer clientId) {
        List<DemandeService> toutesDemandesClient = demandeServiceRepository.findByClientIdUtilisateur(clientId);
        List<DemandeService> demandesAvecPrestataire = toutesDemandesClient.stream()
            .filter(demande -> demande.getStatut() == DemandeService.StatutDemande.PRESTATAIRE_SELECTIONNE ||
                              demande.getStatut() == DemandeService.StatutDemande.EN_COURS ||
                              demande.getStatut() == DemandeService.StatutDemande.TERMINEE)
            .collect(Collectors.toList());
        
        return demandesAvecPrestataire.stream().map(demande -> {
            Map<String, Object> missionMap = new HashMap<>();
            
            Optional<Candidature> candidatureAcceptee = candidatureRepository
                .findByDemandeServiceIdDemande(demande.getIdDemande().intValue())
                .stream()
                .filter(candidature -> candidature.getStatut() == Candidature.StatutCandidature.ACCEPTEE)
                .findFirst();
            
            if (candidatureAcceptee.isPresent()) {
                Candidature candidature = candidatureAcceptee.get();
                
                missionMap.put("id", candidature.getIdCandidature());
                missionMap.put("statut", determinerStatutMission(demande.getStatut()));
                missionMap.put("prixAccepte", candidature.getPrixPropose());
                missionMap.put("dateAcceptation", candidature.getDateReponse());
                
                if (candidature.getPrestataire() != null) {
                    Map<String, Object> prestataireMap = new HashMap<>();
                    prestataireMap.put("idUtilisateur", candidature.getPrestataire().getIdUtilisateur());
                    prestataireMap.put("prenom", candidature.getPrestataire().getPrenom());
                    prestataireMap.put("nom", candidature.getPrestataire().getNom());
                    prestataireMap.put("email", candidature.getPrestataire().getEmail());
                    missionMap.put("prestataire", prestataireMap);
                }
                
                Map<String, Object> demandeMap = new HashMap<>();
                demandeMap.put("idDemande", demande.getIdDemande());
                demandeMap.put("titre", demande.getTitre());
                demandeMap.put("description", demande.getDescription());
                demandeMap.put("dateSouhaitee", demande.getDateSouhaitee());
                demandeMap.put("adresseDepart", demande.getAdresseDepart());
                missionMap.put("demandeService", demandeMap);
            }
            
            return missionMap;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> validerMission(Long missionId, Map<String, Object> validationData) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Mission validée par le client");
        
        return result;
    }

    private void envoyerEmailDemarrageClient(Candidature candidature) {
        try {
            String clientEmail = candidature.getDemandeService().getClient().getEmail();
            String clientNom = candidature.getDemandeService().getClient().getPrenom();
            String prestataireNom = candidature.getPrestataire().getPrenom() + " " + candidature.getPrestataire().getNom();
            String titreDemande = candidature.getDemandeService().getTitre();
            
            String subject = "🚀 Mission démarrée - " + titreDemande;
            String body = String.format(
                "Bonjour %s,\n\n" +
                "Bonne nouvelle ! %s a démarré votre mission \"%s\".\n\n" +
                "Vous pouvez suivre l'avancement de votre mission dans votre espace client EcoDeli.\n\n" +
                "Cordialement,\n" +
                "L'équipe EcoDeli",
                clientNom, prestataireNom, titreDemande
            );
            
            emailService.sendHtmlEmail(clientEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email démarrage mission: " + e.getMessage());
        }
    }

    private void envoyerEmailTerminaisonClient(Candidature candidature, String noteFinale) {
        try {
            String clientEmail = candidature.getDemandeService().getClient().getEmail();
            String clientNom = candidature.getDemandeService().getClient().getPrenom();
            String prestataireNom = candidature.getPrestataire().getPrenom() + " " + candidature.getPrestataire().getNom();
            String titreDemande = candidature.getDemandeService().getTitre();
            
            String subject = "✅ Mission terminée - " + titreDemande;
            String body = String.format(
                "Bonjour %s,\n\n" +
                "%s vient de marquer votre mission \"%s\" comme terminée.\n\n" +
                "%s" +
                "Vous pouvez maintenant consulter les détails dans votre espace client et valider la réalisation.\n\n" +
                "Cordialement,\n" +
                "L'équipe EcoDeli",
                clientNom, 
                prestataireNom, 
                titreDemande,
                noteFinale != null && !noteFinale.trim().isEmpty() ? 
                    "Message du prestataire : \"" + noteFinale + "\"\n\n" : ""
            );
            
            emailService.sendHtmlEmail(clientEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email terminaison mission: " + e.getMessage());
        }
    }
}