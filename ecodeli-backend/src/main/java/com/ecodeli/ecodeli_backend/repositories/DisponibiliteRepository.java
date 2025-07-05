package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.DisponibilitePrestataire;
import com.ecodeli.ecodeli_backend.models.StatutDisponibilite;
import com.ecodeli.ecodeli_backend.models.TypeDisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DisponibiliteRepository extends JpaRepository<DisponibilitePrestataire, Long> {

    List<DisponibilitePrestataire> findByPrestataireIdUtilisateurAndDateDebutBetweenAndStatut(
        Integer prestataireId,
        LocalDateTime debut,
        LocalDateTime fin,
        StatutDisponibilite statut
    );

    boolean existsByPrestataireIdUtilisateurAndDateDebutLessThanEqualAndDateFinGreaterThanEqualAndStatut(
        Integer prestataireId,
        LocalDateTime dateFin,
        LocalDateTime dateDebut,
        StatutDisponibilite statut
    );

    List<DisponibilitePrestataire> findByPrestataireIdUtilisateurAndStatutOrderByDateDebutAsc(
        Integer prestataireId,
        StatutDisponibilite statut
    );

    List<DisponibilitePrestataire> findByPrestataireIdUtilisateurAndTypeAndStatut(
        Integer prestataireId,
        TypeDisponibilite type,
        StatutDisponibilite statut
    );

    @Query("SELECT d FROM DisponibilitePrestataire d WHERE d.prestataire.idUtilisateur = :prestataireId " +
           "AND d.statut = :statut AND d.dateDebut > :maintenant ORDER BY d.dateDebut ASC")
    List<DisponibilitePrestataire> findUpcomingDisponibilites(
        @Param("prestataireId") Integer prestataireId,
        @Param("statut") StatutDisponibilite statut,
        @Param("maintenant") LocalDateTime maintenant
    );

    @Query("SELECT COALESCE(SUM(FUNCTION('TIMESTAMPDIFF', HOUR, d.dateDebut, d.dateFin)), 0) " +
           "FROM DisponibilitePrestataire d WHERE d.prestataire.idUtilisateur = :prestataireId " +
           "AND d.type = :type AND d.statut = :statut " +
           "AND d.dateDebut >= :debut AND d.dateFin <= :fin")
    Long countHeuresDisponibles(
        @Param("prestataireId") Integer prestataireId,
        @Param("type") TypeDisponibilite type,
        @Param("statut") StatutDisponibilite statut,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin
    );

    @Query("SELECT d FROM DisponibilitePrestataire d WHERE d.prestataire.idUtilisateur = :prestataireId " +
           "AND d.statut = :statut AND ((d.dateDebut BETWEEN :debut AND :fin) OR " +
           "(d.dateFin BETWEEN :debut AND :fin) OR " +
           "(d.dateDebut <= :debut AND d.dateFin >= :fin)) ORDER BY d.dateDebut ASC")
    List<DisponibilitePrestataire> findDisponibilitesInRange(
        @Param("prestataireId") Integer prestataireId,
        @Param("statut") StatutDisponibilite statut,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin
    );

    void deleteByPrestataireIdUtilisateurAndDateFinBefore(
        Integer prestataireId,
        LocalDateTime date
    );

    @Query("SELECT d FROM DisponibilitePrestataire d WHERE d.prestataire.idUtilisateur = :prestataireId " +
           "AND d.statut = :statut AND d.id != :excludeId " +
           "AND NOT (d.dateFin <= :dateDebut OR d.dateDebut >= :dateFin)")
    List<DisponibilitePrestataire> findConflictingDisponibilites(
        @Param("prestataireId") Integer prestataireId,
        @Param("statut") StatutDisponibilite statut,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("dateFin") LocalDateTime dateFin,
        @Param("excludeId") Long excludeId
    );
}
