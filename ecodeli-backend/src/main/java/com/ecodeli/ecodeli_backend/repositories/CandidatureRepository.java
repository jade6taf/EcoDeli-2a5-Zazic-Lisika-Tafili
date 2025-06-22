package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Candidature;
import com.ecodeli.ecodeli_backend.models.Candidature.StatutCandidature;
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

    List<Candidature> findByDemandeServiceOrderByDateCandidatureDesc(DemandeService demandeService);

    List<Candidature> findByPrestataireOrderByDateCandidatureDesc(Prestataire prestataire);

    List<Candidature> findByStatutOrderByDateCandidatureDesc(StatutCandidature statut);

    List<Candidature> findByPrestataireAndStatutOrderByDateCandidatureDesc(
        Prestataire prestataire, StatutCandidature statut);

    List<Candidature> findByDemandeServiceAndStatutOrderByDateCandidatureDesc(
        DemandeService demandeService, StatutCandidature statut);

    boolean existsByDemandeServiceAndPrestataire(DemandeService demandeService, Prestataire prestataire);

    Optional<Candidature> findByDemandeServiceAndPrestataire(DemandeService demandeService, Prestataire prestataire);

    Long countByStatut(StatutCandidature statut);

    Long countByPrestataire(Prestataire prestataire);

    Long countByDemandeService(DemandeService demandeService);

    List<Candidature> findByDateCandidatureAfterOrderByDateCandidatureDesc(LocalDateTime date);

    @Query("SELECT c FROM Candidature c WHERE c.prestataire = :prestataire AND c.statut = 'ACCEPTEE' ORDER BY c.dateReponse DESC")
    List<Candidature> findCandidaturesAccepteesPrestataire(@Param("prestataire") Prestataire prestataire);

    @Query("SELECT c FROM Candidature c WHERE c.demandeService = :demandeService AND c.statut = 'EN_ATTENTE' ORDER BY c.dateCandidature ASC")
    List<Candidature> findCandidaturesEnAttenteParDemande(@Param("demandeService") DemandeService demandeService);

    @Query("SELECT d.idDemande, COUNT(c) FROM DemandeService d LEFT JOIN Candidature c ON d.idDemande = c.demandeService.idDemande GROUP BY d.idDemande")
    List<Object[]> getStatistiquesCandidaturesParDemande();
}
