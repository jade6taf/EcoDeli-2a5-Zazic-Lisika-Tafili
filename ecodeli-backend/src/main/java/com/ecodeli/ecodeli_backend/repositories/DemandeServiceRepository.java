package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandeServiceRepository extends JpaRepository<DemandeService, Long> {
    
    /**
     * Récupérer les demandes par statut
     */
    List<DemandeService> findByStatut(DemandeService.StatutDemande statut);
    
    /**
     * Récupérer les demandes d'un client
     */
    List<DemandeService> findByClientIdUtilisateur(Integer clientId);
    
    /**
     * Récupérer les demandes par catégorie de service
     */
    List<DemandeService> findByCategorieService(ServiceType categorieService);
    
    /**
     * Récupérer les demandes publiées par catégorie
     */
    @Query("SELECT d FROM DemandeService d WHERE d.categorieService = :categorie " +
           "AND d.statut = 'PUBLIEE' ORDER BY d.dateCreation DESC")
    List<DemandeService> findPublishedByCategorie(@Param("categorie") ServiceType categorie);
    
    /**
     * Récupérer les demandes récentes (dernière semaine)
     */
    @Query("SELECT d FROM DemandeService d WHERE d.dateCreation >= :dateLimit " +
           "ORDER BY d.dateCreation DESC")
    List<DemandeService> findRecentDemandes(@Param("dateLimit") LocalDateTime dateLimit);
    
    /**
     * Compter les demandes par statut
     */
    @Query("SELECT d.statut, COUNT(d) FROM DemandeService d GROUP BY d.statut")
    List<Object[]> countByStatut();

    /**
     * Récupérer les demandes publiées par catégorie avec pagination et filtres
     */
    @Query("SELECT d FROM DemandeService d WHERE d.categorieService = :categorie " +
           "AND d.statut = 'PUBLIEE' " +
           "AND (:search IS NULL OR LOWER(d.titre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:localisation IS NULL OR LOWER(d.adresseDepart) LIKE LOWER(CONCAT('%', :localisation, '%'))) " +
           "ORDER BY d.dateCreation DESC")
    Page<DemandeService> findPublishedByCategorieWithFilters(
        @Param("categorie") ServiceType categorie,
        @Param("search") String search,
        @Param("localisation") String localisation,
        Pageable pageable);
}
