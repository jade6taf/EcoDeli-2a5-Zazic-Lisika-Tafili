package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Justificatif;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JustificatifRepository extends JpaRepository<Justificatif, Integer> {
    
    /**
     * Récupérer tous les justificatifs d'un utilisateur
     */
    List<Justificatif> findByUtilisateurIdUtilisateur(Integer utilisateurId);
    
    /**
     * Récupérer les justificatifs par statut de validation
     */
    List<Justificatif> findByValidationParAd(Boolean validationParAd);
    
    /**
     * Récupérer les justificatifs par type
     */
    List<Justificatif> findByTypeJustificatif(String typeJustificatif);
    
    /**
     * Récupérer les justificatifs d'un utilisateur par type
     */
    @Query("SELECT j FROM Justificatif j WHERE j.utilisateur.idUtilisateur = :userId " +
           "AND j.typeJustificatif = :type")
    List<Justificatif> findByUserAndType(
        @Param("userId") Integer userId, 
        @Param("type") String type);
    
    /**
     * Récupérer les justificatifs en attente de validation
     */
    @Query("SELECT j FROM Justificatif j WHERE j.validationParAd IS NULL")
    List<Justificatif> findPendingValidation();
    
    /**
     * Récupérer les justificatifs validés
     */
    @Query("SELECT j FROM Justificatif j WHERE j.validationParAd = true")
    List<Justificatif> findValidated();
    
    /**
     * Récupérer les justificatifs rejetés
     */
    @Query("SELECT j FROM Justificatif j WHERE j.validationParAd = false")
    List<Justificatif> findRejected();
    
    /**
     * Récupérer les justificatifs d'un prestataire spécifique
     */
    @Query("SELECT j FROM Justificatif j WHERE j.utilisateur.idUtilisateur = :prestataireId " +
           "AND TYPE(j.utilisateur) = Prestataire")
    List<Justificatif> findByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les justificatifs récents (dernière semaine)
     */
    @Query("SELECT j FROM Justificatif j WHERE j.dateDebut >= :dateLimit")
    List<Justificatif> findRecentJustificatifs(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Compter les justificatifs par statut
     */
    @Query("SELECT " +
           "SUM(CASE WHEN j.validationParAd IS NULL THEN 1 ELSE 0 END) as enAttente, " +
           "SUM(CASE WHEN j.validationParAd = true THEN 1 ELSE 0 END) as valides, " +
           "SUM(CASE WHEN j.validationParAd = false THEN 1 ELSE 0 END) as rejetes " +
           "FROM Justificatif j")
    Object[] countByStatut();
    
    /**
     * Récupérer les justificatifs par type et statut
     */
    @Query("SELECT j FROM Justificatif j WHERE j.typeJustificatif = :type " +
           "AND (:statut IS NULL OR " +
           "     (:statut = 'EN_ATTENTE' AND j.validationParAd IS NULL) OR " +
           "     (:statut = 'VALIDE' AND j.validationParAd = true) OR " +
           "     (:statut = 'REJETE' AND j.validationParAd = false))")
    List<Justificatif> findByTypeAndStatut(
        @Param("type") String type, 
        @Param("statut") String statut);
    
    /**
     * Vérifier si un utilisateur a des justificatifs validés
     */
    @Query("SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END FROM Justificatif j " +
           "WHERE j.utilisateur.idUtilisateur = :userId AND j.validationParAd = true")
    Boolean hasValidatedJustificatifs(@Param("userId") Integer userId);
    
    /**
     * Récupérer les statistiques par type de justificatif
     */
    @Query("SELECT j.typeJustificatif, COUNT(j) FROM Justificatif j " +
           "GROUP BY j.typeJustificatif ORDER BY COUNT(j) DESC")
    List<Object[]> getStatistiquesByType();
}