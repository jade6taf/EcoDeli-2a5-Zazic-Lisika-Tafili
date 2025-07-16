package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.TransactionPortefeuille;
import com.ecodeli.ecodeli_backend.models.TransactionPortefeuille.TypeTransaction;
import com.ecodeli.ecodeli_backend.models.TransactionPortefeuille.StatutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionPortefeuilleRepository extends JpaRepository<TransactionPortefeuille, Long> {
    
    /**
     * Récupérer toutes les transactions d'un portefeuille
     */
    List<TransactionPortefeuille> findByPortefeuilleIdPortefeuilleOrderByDateTransactionDesc(Long portefeuilleId);
    
    /**
     * Récupérer les transactions d'un prestataire par son ID
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findByPrestataireIdOrderByDateDesc(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les transactions par type
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.typeTransaction = :type ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findByPrestataireAndType(
        @Param("prestataireId") Integer prestataireId,
        @Param("type") TypeTransaction type);
    
    /**
     * Récupérer les transactions par statut
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.statutTransaction = :statut ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findByPrestataireAndStatut(
        @Param("prestataireId") Integer prestataireId,
        @Param("statut") StatutTransaction statut);
    
    /**
     * Récupérer les transactions d'une période
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.dateTransaction BETWEEN :dateDebut AND :dateFin ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findByPrestataireAndPeriode(
        @Param("prestataireId") Integer prestataireId,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("dateFin") LocalDateTime dateFin);
    
    /**
     * Calculer le total des crédits d'un prestataire
     */
    @Query("SELECT COALESCE(SUM(t.montant), 0) FROM TransactionPortefeuille t " +
           "WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.typeTransaction IN ('CREDIT_MISSION', 'BONUS', 'REMBOURSEMENT') " +
           "AND t.statutTransaction = 'EFFECTUE'")
    BigDecimal getTotalCredits(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Calculer le total des débits d'un prestataire
     */
    @Query("SELECT COALESCE(SUM(t.montant), 0) FROM TransactionPortefeuille t " +
           "WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.typeTransaction IN ('RETRAIT_PRESTATAIRE', 'FRAIS_SERVICE') " +
           "AND t.statutTransaction = 'EFFECTUE'")
    BigDecimal getTotalDebits(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les dernières transactions d'un prestataire
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findTopByPrestataireOrderByDateDesc(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les transactions en attente
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.statutTransaction = 'EN_ATTENTE' " +
           "ORDER BY t.dateTransaction ASC")
    List<TransactionPortefeuille> findTransactionsEnAttente();
    
    /**
     * Récupérer les transactions échouées récentes
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.statutTransaction = 'ECHOUE' " +
           "AND t.dateTransaction >= :dateLimit ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findTransactionsEchoueesRecentes(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Statistiques des transactions par type
     */
    @Query("SELECT t.typeTransaction, COUNT(t), SUM(t.montant) FROM TransactionPortefeuille t " +
           "WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "AND t.statutTransaction = 'EFFECTUE' " +
           "GROUP BY t.typeTransaction")
    List<Object[]> getStatistiquesParType(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les transactions par référence externe
     */
    Optional<TransactionPortefeuille> findByReferenceExterne(String referenceExterne);
    
    /**
     * Compter les transactions d'un prestataire par statut
     */
    @Query("SELECT t.statutTransaction, COUNT(t) FROM TransactionPortefeuille t " +
           "WHERE t.portefeuille.prestataire.idUtilisateur = :prestataireId " +
           "GROUP BY t.statutTransaction")
    List<Object[]> countByStatutForPrestataire(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les retraits récents
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.typeTransaction = 'RETRAIT_PRESTATAIRE' " +
           "AND t.dateTransaction >= :dateLimit ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findRetraitsRecents(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Calculer le total des commissions EcoDeli
     */
    @Query("SELECT COALESCE(SUM(t.commissionEcodeli), 0) FROM TransactionPortefeuille t " +
           "WHERE t.statutTransaction = 'EFFECTUE' AND t.commissionEcodeli > 0")
    BigDecimal getTotalCommissionsEcodeli();
    
    /**
     * Statistiques globales des transactions
     */
    @Query("SELECT " +
           "COUNT(t) as totalTransactions, " +
           "SUM(CASE WHEN t.statutTransaction = 'EFFECTUE' THEN t.montant ELSE 0 END) as montantTotal, " +
           "SUM(t.commissionEcodeli) as totalCommissions, " +
           "AVG(t.montant) as montantMoyen " +
           "FROM TransactionPortefeuille t")
    Object[] getStatistiquesGlobales();
    
    /**
     * Récupérer les transactions liées à une candidature/mission
     */
    @Query("SELECT t FROM TransactionPortefeuille t WHERE t.candidature.idCandidature = :candidatureId " +
           "ORDER BY t.dateTransaction DESC")
    List<TransactionPortefeuille> findByCandidatureId(@Param("candidatureId") Long candidatureId);
}