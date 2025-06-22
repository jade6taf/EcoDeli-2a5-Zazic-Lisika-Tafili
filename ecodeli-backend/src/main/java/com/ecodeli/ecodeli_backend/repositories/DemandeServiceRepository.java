package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.models.DemandeService.StatutDemande;
import com.ecodeli.ecodeli_backend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandeServiceRepository extends JpaRepository<DemandeService, Long> {

    List<DemandeService> findByClientOrderByDateCreationDesc(Client client);

    List<DemandeService> findByStatutOrderByDateCreationDesc(StatutDemande statut);

    List<DemandeService> findByCategorieServiceOrderByDateCreationDesc(ServiceType categorieService);

    @Query("SELECT d FROM DemandeService d WHERE d.statut IN :statuts ORDER BY d.dateCreation DESC")
    List<DemandeService> findDemandesDisponiblesPourCandidatures(@Param("statuts") List<StatutDemande> statuts);

    List<DemandeService> findByCategorieServiceAndStatutOrderByDateCreationDesc(
        ServiceType categorieService, StatutDemande statut);

    List<DemandeService> findByDateCreationAfterOrderByDateCreationDesc(LocalDateTime date);

    List<DemandeService> findByClientAndStatutOrderByDateCreationDesc(Client client, StatutDemande statut);

    Long countByStatut(StatutDemande statut);

    Long countByClient(Client client);

    @Query("SELECT d FROM DemandeService d WHERE " +
           "(d.budgetMin IS NULL OR d.budgetMin <= :budgetMax) AND " +
           "(d.budgetMax IS NULL OR d.budgetMax >= :budgetMin) " +
           "ORDER BY d.dateCreation DESC")
    List<DemandeService> findByBudgetRange(@Param("budgetMin") Double budgetMin,
                                         @Param("budgetMax") Double budgetMax);
}
