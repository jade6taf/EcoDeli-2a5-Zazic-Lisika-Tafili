package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Service;
import com.ecodeli.ecodeli_backend.models.Service.StatutService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByStatut(StatutService statut);

    List<Service> findByPrestataireOrderByDateCreationDesc(Prestataire prestataire);

    List<Service> findByPrestataireAndStatutOrderByDateCreationDesc(Prestataire prestataire, StatutService statut);

    List<Service> findByClientOrderByDateCreationDesc(Client client);

    List<Service> findByClientAndStatutOrderByDateCreationDesc(Client client, StatutService statut);

    @Query("SELECT s FROM Service s WHERE s.prestataire = :prestataire AND s.statut = 'EN_COURS' ORDER BY s.dateCreation DESC")
    List<Service> findServicesEnCoursByPrestataire(@Param("prestataire") Prestataire prestataire);

    @Query("SELECT s FROM Service s WHERE s.prestataire = :prestataire AND s.statut = 'TERMINE' ORDER BY s.dateFin DESC")
    List<Service> findServicesTerminesByPrestataire(@Param("prestataire") Prestataire prestataire);

    @Query("SELECT s FROM Service s WHERE s.statut = 'EN_COURS' AND s.dateRealisationPrevue < :now")
    List<Service> findServicesEnRetard(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Service s WHERE s.statut = 'EN_COURS' AND s.dateRealisationPrevue BETWEEN :now AND :limite")
    List<Service> findServicesUrgents(@Param("now") LocalDateTime now, @Param("limite") LocalDateTime limite);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.prestataire = :prestataire")
    Long countByPrestataire(@Param("prestataire") Prestataire prestataire);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.prestataire = :prestataire AND s.statut = :statut")
    Long countByPrestataireAndStatut(@Param("prestataire") Prestataire prestataire, @Param("statut") StatutService statut);

    @Query("SELECT COALESCE(SUM(s.prixFinal), 0) FROM Service s WHERE s.prestataire = :prestataire AND s.statut = 'TERMINE'")
    Double calculateChiffresAffairesPrestataire(@Param("prestataire") Prestataire prestataire);

    Optional<Service> findByDemandeService_IdDemande(Long idDemande);

    Optional<Service> findByCandidature_IdCandidature(Long idCandidature);

    @Query("SELECT s FROM Service s WHERE s.dateCreation BETWEEN :debut AND :fin ORDER BY s.dateCreation DESC")
    List<Service> findServicesInPeriod(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
