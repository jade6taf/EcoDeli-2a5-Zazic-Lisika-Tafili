package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Entrepot;
import com.ecodeli.ecodeli_backend.models.Entrepot.StatutEntrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntrepotRepository extends JpaRepository<Entrepot, Integer> {

    List<Entrepot> findByStatut(StatutEntrepot statut);

    List<Entrepot> findByVille(String ville);

    List<Entrepot> findByNombreDePlacesGreaterThanEqual(Integer nombreMinimal);

    List<Entrepot> findByNombreDePlacesLessThanEqual(Integer nombreMaximal);

    @Query("SELECT e FROM Entrepot e WHERE e.nombreDePlaces BETWEEN :min AND :max")
    List<Entrepot> findByPlacesRange(@Param("min") Integer min, @Param("max") Integer max);

    @Query("SELECT e FROM Entrepot e WHERE e.ville = :ville AND e.statut = 'ACTIF'")
    List<Entrepot> findActiveEntrepotsByCity(@Param("ville") String ville);

    long countByStatut(StatutEntrepot statut);

    @Query("SELECT SUM(e.nombreDePlaces) FROM Entrepot e WHERE e.statut = 'ACTIF'")
    Integer calculateTotalAvailablePlaces();
}