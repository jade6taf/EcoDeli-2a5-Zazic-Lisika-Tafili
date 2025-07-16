package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.PrestataireCategorie;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestataireCategorieRepository extends JpaRepository<PrestataireCategorie, Long> {
    
    /**
     * Récupérer toutes les validations d'un prestataire
     */
    List<PrestataireCategorie> findByPrestataireIdUtilisateur(Integer prestataireId);
    
    /**
     * Récupérer les validations par catégorie de service
     */
    List<PrestataireCategorie> findByCategorieService(ServiceType categorieService);
    
    /**
     * Récupérer les validations par statut
     */
    List<PrestataireCategorie> findByStatutValidation(PrestataireCategorie.StatutValidation statut);
    
    /**
     * Trouver une validation spécifique prestataire/catégorie
     */
    Optional<PrestataireCategorie> findByPrestataireIdUtilisateurAndCategorieService(
        Integer prestataireId, ServiceType categorieService);
    
    /**
     * Récupérer les prestataires validés pour une catégorie donnée
     */
    @Query("SELECT pc FROM PrestataireCategorie pc WHERE pc.statutValidation = :statut " +
           "AND pc.categorieService = :categorie")
    List<PrestataireCategorie> findValidatedByCategory(
        @Param("statut") PrestataireCategorie.StatutValidation statut,
        @Param("categorie") ServiceType categorie);
    
    /**
     * Récupérer les validations avec tarif défini
     */
    @Query("SELECT pc FROM PrestataireCategorie pc WHERE pc.tarifHoraire IS NOT NULL " +
           "AND pc.tarifHoraire > 0")
    List<PrestataireCategorie> findWithTarif();
    
    /**
     * Récupérer les validations sans tarif
     */
    @Query("SELECT pc FROM PrestataireCategorie pc WHERE pc.tarifHoraire IS NULL " +
           "OR pc.tarifHoraire <= 0")
    List<PrestataireCategorie> findWithoutTarif();
    
    /**
     * Compter les validations par statut pour un prestataire
     */
    @Query("SELECT COUNT(pc) FROM PrestataireCategorie pc WHERE pc.prestataire.idUtilisateur = :prestataireId " +
           "AND pc.statutValidation = :statut")
    Long countByPrestataireAndStatut(
        @Param("prestataireId") Integer prestataireId, 
        @Param("statut") PrestataireCategorie.StatutValidation statut);
    
    /**
     * Vérifier si un prestataire a au moins une catégorie validée
     */
    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM PrestataireCategorie pc " +
           "WHERE pc.prestataire.idUtilisateur = :prestataireId " +
           "AND pc.statutValidation = 'VALIDE'")
    Boolean hasValidatedCategory(@Param("prestataireId") Integer prestataireId);
    
    /**
     * Récupérer les validations récentes (dernière semaine)
     */
    @Query("SELECT pc FROM PrestataireCategorie pc WHERE pc.dateValidation >= :dateLimit")
    List<PrestataireCategorie> findRecentValidations(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Statistiques par catégorie
     */
    @Query("SELECT pc.categorieService, COUNT(pc) FROM PrestataireCategorie pc " +
           "WHERE pc.statutValidation = :statut GROUP BY pc.categorieService")
    List<Object[]> countByStatutAndCategorie(@Param("statut") PrestataireCategorie.StatutValidation statut);
}