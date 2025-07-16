package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationMissionRepository evaluationRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaiementFictifService paiementFictifService;

    @Autowired
    private PrixCalculService prixCalculService;

    @Transactional
    public Map<String, Object> creerEvaluation(Long candidatureId, Map<String, Object> evaluationData) {
        Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
        if (!candidatureOpt.isPresent()) {
            throw new RuntimeException("Candidature non trouvée avec ID: " + candidatureId);
        }
        
        Candidature candidature = candidatureOpt.get();
        
        if (candidature.getDemandeService().getStatut() != DemandeService.StatutDemande.TERMINEE) {
            throw new RuntimeException("La mission doit être terminée avant d'être évaluée");
        }
        
        if (evaluationRepository.existsByCandidature(candidatureId)) {
            throw new RuntimeException("Cette mission a déjà été évaluée");
        }
        
        Integer noteGlobale = (Integer) evaluationData.get("noteGlobale");
        Integer noteQualite = (Integer) evaluationData.get("noteQualite");
        Integer noteDelais = (Integer) evaluationData.get("noteDelais");
        Integer noteCommunication = (Integer) evaluationData.get("noteCommunication");
        Integer noteProfessionnalisme = (Integer) evaluationData.get("noteProfessionnalisme");
        String commentaire = (String) evaluationData.get("commentaire");
        
        validerNotes(noteGlobale, noteQualite, noteDelais, noteCommunication, noteProfessionnalisme);
        
        EvaluationMission evaluation = new EvaluationMission();
        evaluation.setCandidature(candidature);
        evaluation.setClient(candidature.getDemandeService().getClient());
        evaluation.setPrestataire(candidature.getPrestataire());
        evaluation.setNoteGlobale(noteGlobale);
        evaluation.setNoteQualite(noteQualite);
        evaluation.setNoteDelais(noteDelais);
        evaluation.setNoteCommunication(noteCommunication);
        evaluation.setNoteProfessionnalisme(noteProfessionnalisme);
        evaluation.setCommentaire(commentaire);
        evaluation.setDateEvaluation(LocalDateTime.now());
        
        evaluation = evaluationRepository.save(evaluation);
        
        Map<String, Object> paiementResult = null;
        try {
            BigDecimal montantMission = prixCalculService.calculerPrixMission(candidatureId);
            
            paiementResult = paiementFictifService.simulerPaiementMission(candidatureId, montantMission);
            
            if (!(Boolean) paiementResult.get("success")) {
                System.err.println("Erreur paiement mission: " + paiementResult.get("error"));
            }
        } catch (Exception e) {
            System.err.println("Erreur déclenchement paiement: " + e.getMessage());
        }
        
        try {
            envoyerEmailNouvelleEvaluation(evaluation);
        } catch (Exception e) {
            System.err.println("Erreur envoi email évaluation: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Évaluation créée et mission payée avec succès");
        result.put("evaluationId", evaluation.getIdEvaluation());
        result.put("noteGlobale", evaluation.getNoteGlobale());
        result.put("noteLabel", evaluation.getNoteGlobaleLabel());
        
        if (paiementResult != null && (Boolean) paiementResult.get("success")) {
            result.put("paiementEffectue", true);
            result.put("montantPaye", paiementResult.get("montantTotal"));
            result.put("montantPrestataire", paiementResult.get("montantPrestataire"));
            result.put("commissionEcodeli", paiementResult.get("commissionEcodeli"));
        } else {
            result.put("paiementEffectue", false);
            result.put("paiementError", paiementResult != null ? paiementResult.get("error") : "Erreur inconnue");
        }
        
        return result;
    }

    public Map<String, Object> getStatistiquesPrestataire(Integer prestataireId) {
        Map<String, Object> stats = new HashMap<>();
    
        Double noteMoyenne = evaluationRepository.getNoteMoyenneByPrestataire(prestataireId);
        stats.put("noteMoyenne", noteMoyenne != null ? Math.round(noteMoyenne * 10.0) / 10.0 : 0.0);
        
        Long totalEvaluations = evaluationRepository.countEvaluationsByPrestataire(prestataireId);
        stats.put("totalEvaluations", totalEvaluations != null ? totalEvaluations : 0L);
        
        Object[] notesCriteres = evaluationRepository.getNotesMoyennesCriteresByPrestataire(prestataireId);
        if (notesCriteres != null && notesCriteres.length == 4) {
            stats.put("noteQualite", notesCriteres[0] != null ? Math.round((Double)notesCriteres[0] * 10.0) / 10.0 : 0.0);
            stats.put("noteDelais", notesCriteres[1] != null ? Math.round((Double)notesCriteres[1] * 10.0) / 10.0 : 0.0);
            stats.put("noteCommunication", notesCriteres[2] != null ? Math.round((Double)notesCriteres[2] * 10.0) / 10.0 : 0.0);
            stats.put("noteProfessionnalisme", notesCriteres[3] != null ? Math.round((Double)notesCriteres[3] * 10.0) / 10.0 : 0.0);
        } else {
            stats.put("noteQualite", 0.0);
            stats.put("noteDelais", 0.0);
            stats.put("noteCommunication", 0.0);
            stats.put("noteProfessionnalisme", 0.0);
        }
        
        List<Object[]> repartitionNotes = evaluationRepository.getStatistiquesNotesGlobalesByPrestataire(prestataireId);
        Map<Integer, Long> repartition = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            repartition.put(i, 0L);
        }
        for (Object[] row : repartitionNotes) {
            Integer note = (Integer) row[0];
            Long count = (Long) row[1];
            repartition.put(note, count);
        }
        stats.put("repartitionNotes", repartition);
        
        long evaluationsPositives = repartition.get(4) + repartition.get(5);
        double pourcentagePositif = totalEvaluations > 0 ? 
            (double) evaluationsPositives / totalEvaluations * 100 : 0.0;
        stats.put("pourcentagePositif", Math.round(pourcentagePositif * 10.0) / 10.0);
        
        return stats;
    }

    public List<Map<String, Object>> getDernieresEvaluationsAvecCommentaires(Integer prestataireId, int limit) {
        List<EvaluationMission> evaluations = evaluationRepository
            .findEvaluationsWithCommentsByPrestataire(prestataireId);
        
        return evaluations.stream()
            .limit(limit)
            .map(this::formatEvaluationForResponse)
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEvaluationsPrestataire(Integer prestataireId) {
        List<EvaluationMission> evaluations = evaluationRepository
            .findByPrestataireIdUtilisateurOrderByDateEvaluationDesc(prestataireId);
        
        return evaluations.stream()
            .map(this::formatEvaluationForResponse)
            .collect(Collectors.toList());
    }

    public Map<String, Object> getEvaluationByCandidature(Long candidatureId) {
        Optional<EvaluationMission> evaluationOpt = evaluationRepository.findByCandidatureIdCandidature(candidatureId);
        
        if (evaluationOpt.isPresent()) {
            return formatEvaluationForResponse(evaluationOpt.get());
        }
        
        return null;
    }

    private void validerNotes(Integer... notes) {
        for (Integer note : notes) {
            if (note == null || note < 1 || note > 5) {
                throw new RuntimeException("Toutes les notes doivent être entre 1 et 5");
            }
        }
    }

    private Map<String, Object> formatEvaluationForResponse(EvaluationMission evaluation) {
        Map<String, Object> evalMap = new HashMap<>();
        
        evalMap.put("idEvaluation", evaluation.getIdEvaluation());
        evalMap.put("noteGlobale", evaluation.getNoteGlobale());
        evalMap.put("noteGlobaleLabel", evaluation.getNoteGlobaleLabel());
        evalMap.put("noteQualite", evaluation.getNoteQualite());
        evalMap.put("noteDelais", evaluation.getNoteDelais());
        evalMap.put("noteCommunication", evaluation.getNoteCommunication());
        evalMap.put("noteProfessionnalisme", evaluation.getNoteProfessionnalisme());
        evalMap.put("commentaire", evaluation.getCommentaire());
        evalMap.put("dateEvaluation", evaluation.getDateEvaluation());
        evalMap.put("sentiment", evaluation.getSentiment());
        
        if (evaluation.getClient() != null) {
            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("prenom", evaluation.getClient().getPrenom());
            clientMap.put("nom", evaluation.getClient().getNom());
            String email = evaluation.getClient().getEmail();
            if (email != null && email.contains("@")) {
                String[] parts = email.split("@");
                clientMap.put("emailMasque", parts[0].charAt(0) + "***@" + parts[1]);
            }
            evalMap.put("client", clientMap);
        }
        
        if (evaluation.getCandidature() != null && evaluation.getCandidature().getDemandeService() != null) {
            Map<String, Object> missionMap = new HashMap<>();
            DemandeService demande = evaluation.getCandidature().getDemandeService();
            missionMap.put("titre", demande.getTitre());
            missionMap.put("description", demande.getDescription());
            missionMap.put("categorieService", demande.getCategorieService().name());
            evalMap.put("mission", missionMap);
        }
        
        return evalMap;
    }

    private void envoyerEmailNouvelleEvaluation(EvaluationMission evaluation) {
        try {
            String prestataireEmail = evaluation.getPrestataire().getEmail();
            String prestataireNom = evaluation.getPrestataire().getPrenom();
            String titreMission = evaluation.getCandidature().getDemandeService().getTitre();
            String noteLabel = evaluation.getNoteGlobaleLabel();
            
            String subject = "⭐ Nouvelle évaluation reçue - " + titreMission;
            String body = String.format(
                "<h2>Nouvelle évaluation reçue !</h2>" +
                "<p>Bonjour %s,</p>" +
                "<p>Vous avez reçu une nouvelle évaluation pour votre mission <strong>\"%s\"</strong>.</p>" +
                "<div style=\"background: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                "<h3 style=\"margin: 0 0 10px 0; color: #1e40af;\">Note globale : ⭐ %d/5 (%s)</h3>" +
                "<div style=\"font-size: 14px; color: #374151;\">" +
                "<p><strong>Qualité :</strong> %d/5 ⭐</p>" +
                "<p><strong>Délais :</strong> %d/5 ⭐</p>" +
                "<p><strong>Communication :</strong> %d/5 ⭐</p>" +
                "<p><strong>Professionnalisme :</strong> %d/5 ⭐</p>" +
                "</div>" +
                "%s" +
                "</div>" +
                "<p>Vous pouvez consulter toutes vos évaluations dans votre espace prestataire EcoDeli.</p>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/prestataire/evaluations\" " +
                "style=\"background: #10b981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px;\">Voir mes évaluations</a>" +
                "</div>" +
                "<p>Continuez votre excellent travail !</p>" +
                "<p>L'équipe EcoDeli</p>",
                prestataireNom,
                titreMission,
                evaluation.getNoteGlobale(),
                noteLabel,
                evaluation.getNoteQualite(),
                evaluation.getNoteDelais(),
                evaluation.getNoteCommunication(),
                evaluation.getNoteProfessionnalisme(),
                evaluation.getCommentaire() != null && !evaluation.getCommentaire().trim().isEmpty() ?
                    "<p><strong>Commentaire :</strong><br><em>\"" + evaluation.getCommentaire() + "\"</em></p>" : ""
            );
            
            emailService.sendHtmlEmail(prestataireEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email nouvelle évaluation: " + e.getMessage());
        }
    }
}