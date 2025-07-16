package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.AnnonceCommercant;
import com.ecodeli.ecodeli_backend.models.CategorieAnnonce;
import com.ecodeli.ecodeli_backend.models.Commercant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnonceCommercantRepository extends JpaRepository<AnnonceCommercant, Integer> {
    
    List<AnnonceCommercant> findByCommercantOrderByDateCreationDesc(Commercant commercant);
    
    List<AnnonceCommercant> findByCommercant_IdUtilisateurOrderByDateCreationDesc(Integer commercantId);
    
    List<AnnonceCommercant> findByCategorieOrderByDateCreationDesc(CategorieAnnonce categorie);
    
    List<AnnonceCommercant> findByStatutOrderByDateCreationDesc(AnnonceCommercant.StatutAnnonce statut);
    
    List<AnnonceCommercant> findByStatutApprobationOrderByDateCreationDesc(AnnonceCommercant.StatutApprobation statutApprobation);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = :categorie AND a.statut = :statut ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findByCategorieAndStatut(@Param("categorie") CategorieAnnonce categorie, 
                                                    @Param("statut") AnnonceCommercant.StatutAnnonce statut);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.commercant.idUtilisateur = :commercantId AND a.categorie = :categorie ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findByCommercantAndCategorie(@Param("commercantId") Integer commercantId, 
                                                        @Param("categorie") CategorieAnnonce categorie);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.statut = 'ACTIVE' AND (a.dateExpiration IS NULL OR a.dateExpiration > :now) ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findActiveAnnouncements(@Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = :categorie AND a.statut = 'ACTIVE' AND (a.dateExpiration IS NULL OR a.dateExpiration > :now) ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findActiveByCategorieAnnouncements(@Param("categorie") CategorieAnnonce categorie, 
                                                              @Param("now") LocalDateTime now);

    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'LIVRAISON_PONCTUELLE' AND a.dateLivraisonPrecise BETWEEN :startDate AND :endDate AND a.statut = 'ACTIVE' ORDER BY a.dateLivraisonPrecise ASC")
    List<AnnonceCommercant> findLivraisonPonctuelleByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'LIVRAISON_PONCTUELLE' AND a.disponibiliteTempsReel = true AND a.statut = 'ACTIVE' ORDER BY a.dateLivraisonPrecise ASC")
    List<AnnonceCommercant> findLivraisonPonctuelleRealTimeAvailable();
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'SERVICE_CHARIOT' AND a.gestionDisponibiliteContinue = true AND a.statut = 'ACTIVE' ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findServiceChariotContinuousAvailability();
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'SERVICE_CHARIOT' AND a.zoneCouverture LIKE %:zone% AND a.statut = 'ACTIVE' ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findServiceChariotByZone(@Param("zone") String zone);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'TRANSPORT_MARCHANDISES' AND a.frequenceTransport = :frequence AND a.statut = 'ACTIVE' ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findTransportMarchandisesByFrequence(@Param("frequence") AnnonceCommercant.FrequenceTransport frequence);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.categorie = 'TRANSPORT_MARCHANDISES' AND a.optimisationRoute = true AND a.statut = 'ACTIVE' ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findTransportMarchandisesWithRouteOptimization();
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.statutApprobation = 'EN_ATTENTE' ORDER BY a.dateCreation ASC")
    List<AnnonceCommercant> findPendingApproval();
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.statutApprobation = 'EN_ATTENTE' AND a.categorie = :categorie ORDER BY a.dateCreation ASC")
    List<AnnonceCommercant> findPendingApprovalByCategorie(@Param("categorie") CategorieAnnonce categorie);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.adminApprobateur.idUtilisateur = :adminId ORDER BY a.dateApprobation DESC")
    List<AnnonceCommercant> findApprovedByAdmin(@Param("adminId") Integer adminId);
    
    @Query("SELECT COUNT(a) FROM AnnonceCommercant a WHERE a.categorie = :categorie")
    Long countByCategorie(@Param("categorie") CategorieAnnonce categorie);
    
    @Query("SELECT COUNT(a) FROM AnnonceCommercant a WHERE a.statut = :statut")
    Long countByStatut(@Param("statut") AnnonceCommercant.StatutAnnonce statut);
    
    @Query("SELECT COUNT(a) FROM AnnonceCommercant a WHERE a.statutApprobation = :statutApprobation")
    Long countByStatutApprobation(@Param("statutApprobation") AnnonceCommercant.StatutApprobation statutApprobation);
    
    @Query("SELECT a.categorie, COUNT(a) FROM AnnonceCommercant a GROUP BY a.categorie")
    List<Object[]> getStatisticsByCategorie();
    
    @Query("SELECT a.statut, COUNT(a) FROM AnnonceCommercant a GROUP BY a.statut")
    List<Object[]> getStatisticsByStatut();
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE " +
           "(:categorie IS NULL OR a.categorie = :categorie) AND " +
           "(:statut IS NULL OR a.statut = :statut) AND " +
           "(:commercantId IS NULL OR a.commercant.idUtilisateur = :commercantId) AND " +
           "(:searchTerm IS NULL OR LOWER(a.titre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.dateCreation DESC")
    List<AnnonceCommercant> findWithFilters(@Param("categorie") CategorieAnnonce categorie,
                                           @Param("statut") AnnonceCommercant.StatutAnnonce statut,
                                           @Param("commercantId") Integer commercantId,
                                           @Param("searchTerm") String searchTerm);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.dateExpiration <= :now AND a.statut = 'ACTIVE'")
    List<AnnonceCommercant> findExpiredAnnouncements(@Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM AnnonceCommercant a WHERE a.dateExpiration BETWEEN :now AND :futureDate AND a.statut = 'ACTIVE'")
    List<AnnonceCommercant> findExpiringAnnouncements(@Param("now") LocalDateTime now, 
                                                     @Param("futureDate") LocalDateTime futureDate);
    
    boolean existsByCommercantAndTitreAndCategorie(Commercant commercant, String titre, CategorieAnnonce categorie);
    
    @Query("SELECT COUNT(a) FROM AnnonceCommercant a WHERE a.commercant.idUtilisateur = :commercantId AND a.statut IN ('ACTIVE', 'PUBLIEE')")
    Long countActiveAnnouncementsByCommercant(@Param("commercantId") Integer commercantId);
}