package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.PortefeuillePrestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortefeuilleRepository extends JpaRepository<PortefeuillePrestataire, Long> {
    
    /**
     * Récupérer le portefeuille d'un prestataire par son ID
     */
    Optional<PortefeuillePrestataire> findByPrestataireIdUtilisateur(Integer prestataireId);
    
    /**
     * Vérifier si un prestataire a déjà un portefeuille
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PortefeuillePrestataire p " +
           "WHERE p.prestataire.idUtilisateur = :prestataireId")
    Boolean existsByPrestataireId(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les portefeuilles avec un solde minimum
     */
    @Query("SELECT p FROM PortefeuillePrestataire p WHERE p.soldeDisponible >= :soldeMinimum " +
           "ORDER BY p.soldeDisponible DESC")
    List<PortefeuillePrestataire> findBysoldeDisponibleGreaterThanEqual(@Param("soldeMinimum") BigDecimal soldeMinimum);
    
    /**
     * Récupérer les portefeuilles par nombre de transactions
     */
    @Query("SELECT p FROM PortefeuillePrestataire p WHERE p.nombreTransactions >= :minTransactions " +
           "ORDER BY p.nombreTransactions DESC")
    List<PortefeuillePrestataire> findByNombreTransactionsGreaterThanEqual(@Param("minTransactions") Integer minTransactions);
    
    /**
     * Statistiques globales des portefeuilles
     */
    @Query("SELECT " +
           "SUM(p.soldeDisponible) as totalSoldeDisponible, " +
           "SUM(p.soldeEnAttente) as totalSoldeEnAttente, " +
           "SUM(p.totalGagne) as totalGagne, " +
           "SUM(p.totalRetire) as totalRetire, " +
           "COUNT(p) as nombrePortefeuilles " +
           "FROM PortefeuillePrestataire p")
    Object[] getStatistiquesGlobales();
    
    /**
     * Récupérer les portefeuilles actifs (avec des transactions)
     */
    @Query("SELECT p FROM PortefeuillePrestataire p WHERE p.nombreTransactions > 0 " +
           "ORDER BY p.dateModification DESC")
    List<PortefeuillePrestataire> findPortefeuillesActifs();
    
    /**
     * Récupérer les portefeuilles par montant total gagné
     */
    @Query("SELECT p FROM PortefeuillePrestataire p WHERE p.totalGagne >= :montantMinimum " +
           "ORDER BY p.totalGagne DESC")
    List<PortefeuillePrestataire> findByTotalGagneGreaterThanEqual(@Param("montantMinimum") BigDecimal montantMinimum);
    
    /**
     * Calculer le total des gains par mois
     */
    @Query("SELECT YEAR(p.dateModification), MONTH(p.dateModification), SUM(p.totalGagne) " +
           "FROM PortefeuillePrestataire p " +
           "GROUP BY YEAR(p.dateModification), MONTH(p.dateModification) " +
           "ORDER BY YEAR(p.dateModification) DESC, MONTH(p.dateModification) DESC")
    List<Object[]> getGainsParMois();
    
    /**
     * Récupérer les prestataires les plus actifs
     */
    @Query("SELECT p.prestataire.idUtilisateur, p.nombreTransactions, p.totalGagne " +
           "FROM PortefeuillePrestataire p " +
           "WHERE p.nombreTransactions > 0 " +
           "ORDER BY p.nombreTransactions DESC, p.totalGagne DESC")
    List<Object[]> getPrestatairesLesPlusActifs();
    
    /**
     * Récupérer les portefeuilles nécessitant une attention (solde négatif ou erreurs)
     */
    @Query("SELECT p FROM PortefeuillePrestataire p " +
           "WHERE p.soldeDisponible < 0 OR p.soldeEnAttente < 0")
    List<PortefeuillePrestataire> findPortefeuillesAvecProblemes();
}