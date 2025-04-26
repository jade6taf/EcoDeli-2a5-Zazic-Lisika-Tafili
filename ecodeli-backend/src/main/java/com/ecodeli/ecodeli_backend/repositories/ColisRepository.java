package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Colis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColisRepository extends JpaRepository<Colis, Integer> {

    List<Colis> findByPoidsLessThan(BigDecimal poids);
    List<Colis> findByPoidsGreaterThan(BigDecimal poids);

    List<Colis> findByLongueurLessThan(BigDecimal longueur);
    List<Colis> findByLargeurLessThan(BigDecimal largeur);
    List<Colis> findByHauteurLessThan(BigDecimal hauteur);

    List<Colis> findByFragile(Boolean fragile);

    List<Colis> findByDescriptionContainingIgnoreCase(String keyword);

    @Query("SELECT c FROM Colis c WHERE c.poids BETWEEN :poidsMin AND :poidsMax")
    List<Colis> findByPoidsRange(@Param("poidsMin") BigDecimal poidsMin, @Param("poidsMax") BigDecimal poidsMax);
}