package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.EvaluationMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationMissionRepository extends JpaRepository<EvaluationMission, Long> {
    
    /**
     * Récupérer l'évaluation d'une candidature/mission
     */
    Optional<EvaluationMission> findByCandidatureIdCandidature(Long candidatureId);
    
    /**
     * Récupérer toutes les évaluations d'un prestataire
     */
    List<EvaluationMission> findByPrestataireIdUtilisateurOrderByDateEvaluationDesc(Integer prestataireId);
    
    /**
     * Récupérer toutes les évaluations données par un client
     */
    List<EvaluationMission> findByClientIdUtilisateurOrderByDateEvaluationDesc(Integer clientId);
    
    /**
     * Calculer la note moyenne d'un prestataire
     */
    @Query("SELECT AVG(e.noteGlobale) FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId")
    Double getNoteMoyenneByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Calculer les notes moyennes par critère pour un prestataire
     */
    @Query("SELECT " +
           "AVG(e.noteQualite) as noteQualite, " +
           "AVG(e.noteDelais) as noteDelais, " +
           "AVG(e.noteCommunication) as noteCommunication, " +
           "AVG(e.noteProfessionnalisme) as noteProfessionnalisme " +
           "FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId")
    Object[] getNotesMoyennesCriteresByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Compter le nombre total d'évaluations d'un prestataire
     */
    @Query("SELECT COUNT(e) FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId")
    Long countEvaluationsByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les X dernières évaluations d'un prestataire
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId " +
           "ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findTopEvaluationsByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les évaluations avec commentaire d'un prestataire
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId " +
           "AND e.commentaire IS NOT NULL AND e.commentaire != '' " +
           "ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findEvaluationsWithCommentsByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Statistiques par note globale pour un prestataire
     */
    @Query("SELECT e.noteGlobale, COUNT(e) FROM EvaluationMission e " +
           "WHERE e.prestataire.idUtilisateur = :prestataireId " +
           "GROUP BY e.noteGlobale ORDER BY e.noteGlobale DESC")
    List<Object[]> getStatistiquesNotesGlobalesByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les évaluations récentes (dernière semaine)
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.dateEvaluation >= :dateLimit " +
           "ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findRecentEvaluations(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Récupérer les évaluations positives d'un prestataire (note >= 4)
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId " +
           "AND e.noteGlobale >= 4 ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findPositiveEvaluationsByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les évaluations négatives d'un prestataire (note <= 2)
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.prestataire.idUtilisateur = :prestataireId " +
           "AND e.noteGlobale <= 2 ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findNegativeEvaluationsByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Vérifier si une candidature a déjà été évaluée
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EvaluationMission e " +
           "WHERE e.candidature.idCandidature = :candidatureId")
    Boolean existsByCandidature(@Param("candidatureId") Long candidatureId);
    
    /**
     * Statistiques globales des évaluations
     */
    @Query("SELECT " +
           "COUNT(e) as totalEvaluations, " +
           "AVG(e.noteGlobale) as noteMoyenneGlobale, " +
           "SUM(CASE WHEN e.noteGlobale >= 4 THEN 1 ELSE 0 END) as evaluationsPositives, " +
           "SUM(CASE WHEN e.noteGlobale <= 2 THEN 1 ELSE 0 END) as evaluationsNegatives " +
           "FROM EvaluationMission e")
    Object[] getStatistiquesGlobales();
    
    /**
     * Récupérer les prestataires les mieux notés
     */
    @Query("SELECT e.prestataire.idUtilisateur, AVG(e.noteGlobale) as moyenne, COUNT(e) as nombre " +
           "FROM EvaluationMission e " +
           "GROUP BY e.prestataire.idUtilisateur " +
           "HAVING COUNT(e) >= :minEvaluations " +
           "ORDER BY moyenne DESC")
    List<Object[]> getTopPrestatairesByNote(@Param("minEvaluations") Long minEvaluations);
    
    /**
     * Récupérer les évaluations par plage de notes
     */
    @Query("SELECT e FROM EvaluationMission e WHERE e.noteGlobale BETWEEN :noteMin AND :noteMax " +
           "ORDER BY e.dateEvaluation DESC")
    List<EvaluationMission> findByNoteRange(@Param("noteMin") Integer noteMin, @Param("noteMax") Integer noteMax);
}