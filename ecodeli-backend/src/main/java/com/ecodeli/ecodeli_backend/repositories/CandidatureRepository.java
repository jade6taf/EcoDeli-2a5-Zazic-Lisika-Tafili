package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Candidature;
import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    
    /**
     * Récupérer toutes les candidatures d'un prestataire
     */
    List<Candidature> findByPrestataireIdUtilisateur(Integer prestataireId);
    
    /**
     * Récupérer les candidatures pour une demande de service
     */
    List<Candidature> findByDemandeServiceIdDemande(Integer demandeId);
    
    /**
     * Récupérer les candidatures par statut
     */
    List<Candidature> findByStatut(Candidature.StatutCandidature statut);
    
    /**
     * Vérifier si un prestataire a déjà candidaté pour une demande
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidature c " +
           "WHERE c.prestataire.idUtilisateur = :prestataireId AND c.demandeService.idDemande = :demandeId")
    Boolean existsByPrestataireAndDemande(
        @Param("prestataireId") Integer prestataireId,
        @Param("demandeId") Long demandeId);
    
    /**
     * Récupérer une candidature spécifique prestataire/demande
     */
    Optional<Candidature> findByPrestataireIdUtilisateurAndDemandeServiceIdDemande(
        Integer prestataireId, Long demandeId);
    
    /**
     * Récupérer les candidatures d'un prestataire par statut
     */
    @Query("SELECT c FROM Candidature c WHERE c.prestataire.idUtilisateur = :prestataireId " +
           "AND c.statut = :statut ORDER BY c.dateCandidature DESC")
    List<Candidature> findByPrestataireAndStatut(
        @Param("prestataireId") Integer prestataireId,
        @Param("statut") Candidature.StatutCandidature statut);
    
    /**
     * Récupérer les candidatures récentes d'un prestataire
     */
    @Query("SELECT c FROM Candidature c WHERE c.prestataire.idUtilisateur = :prestataireId " +
           "ORDER BY c.dateCandidature DESC")
    List<Candidature> findByPrestataireOrderByDateDesc(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les candidatures en attente pour une demande
     */
    @Query("SELECT c FROM Candidature c WHERE c.demandeService.idDemande = :demandeId " +
           "AND c.statut = 'EN_ATTENTE' ORDER BY c.dateCandidature ASC")
    List<Candidature> findPendingByDemande(@Param("demandeId") Integer demandeId);
    
    /**
     * Compter les candidatures par statut pour un prestataire
     */
    @Query("SELECT c.statut, COUNT(c) FROM Candidature c " +
           "WHERE c.prestataire.idUtilisateur = :prestataireId GROUP BY c.statut")
    List<Object[]> countByStatutForPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les candidatures récentes (dernière semaine)
     */
    @Query("SELECT c FROM Candidature c WHERE c.dateCandidature >= :dateLimit " +
           "ORDER BY c.dateCandidature DESC")
    List<Candidature> findRecentCandidatures(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Récupérer les candidatures acceptées d'un prestataire
     */
    @Query("SELECT c FROM Candidature c WHERE c.prestataire.idUtilisateur = :prestataireId " +
           "AND c.statut = 'ACCEPTEE' ORDER BY c.dateReponse DESC")
    List<Candidature> findAcceptedByPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Statistiques globales des candidatures
     */
    @Query("SELECT " +
           "SUM(CASE WHEN c.statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) as enAttente, " +
           "SUM(CASE WHEN c.statut = 'ACCEPTEE' THEN 1 ELSE 0 END) as acceptees, " +
           "SUM(CASE WHEN c.statut = 'REFUSEE' THEN 1 ELSE 0 END) as refusees " +
           "FROM Candidature c")
    Object[] getStatistiquesGlobales();
    
    /**
     * Récupérer les candidatures avec prix dans une fourchette
     */
    @Query("SELECT c FROM Candidature c WHERE c.prixPropose BETWEEN :prixMin AND :prixMax " +
           "ORDER BY c.prixPropose ASC")
    List<Candidature> findByPrixRange(
        @Param("prixMin") java.math.BigDecimal prixMin,
        @Param("prixMax") java.math.BigDecimal prixMax);
}