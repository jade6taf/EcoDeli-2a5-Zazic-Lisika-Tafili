package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Contrat;
import com.ecodeli.ecodeli_backend.models.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    List<Contrat> findByCommercantIdUtilisateur(Integer idCommercant);

    List<Contrat> findByAdminIdUtilisateur(Integer idAdmin);

    List<Contrat> findByStatut(StatutContrat statut);

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    List<Contrat> findByCommercantIdUtilisateurAndStatut(Integer idCommercant, StatutContrat statut);

    long countByStatut(StatutContrat statut);

    long countByCommercantIdUtilisateur(Integer idCommercant);

    @Query("SELECT c.statut as statut, COUNT(c) as count FROM Contrat c GROUP BY c.statut")
    List<Object[]> getStatistiquesParStatut();

    @Query("SELECT c FROM Contrat c WHERE c.dateCreation >= :dateLimit ORDER BY c.dateCreation DESC")
    List<Contrat> findContratsRecents(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT c FROM Contrat c WHERE c.dateFin <= :dateLimit AND c.statut = :statut")
    List<Contrat> findContratsExpirantBientot(@Param("dateLimit") LocalDate dateLimit, @Param("statut") StatutContrat statut);

    boolean existsByNumeroContrat(String numeroContrat);

    @Query("SELECT c.numeroContrat FROM Contrat c WHERE c.numeroContrat LIKE :pattern ORDER BY c.numeroContrat DESC")
    List<String> findLastContractNumbersForYear(@Param("pattern") String pattern);
}
