package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ColisRepository extends JpaRepository<Colis, Integer> {

    List<Colis> findByExpediteur(Utilisateur expediteur);

    List<Colis> findByExpediteurIdUtilisateur(Integer idExpediteur);

    List<Colis> findByDestinataire(Utilisateur destinataire);

    List<Colis> findByDestinataireIdUtilisateur(Integer idDestinataire);

    List<Colis> findByType(String type);

    List<Colis> findByDateDebutBetween(LocalDateTime dateDebut1, LocalDateTime dateDebut2);

    List<Colis> findByDateFinBetween(LocalDateTime dateFin1, LocalDateTime dateFin2);

    List<Colis> findByDateDebutBefore(LocalDateTime date);

    List<Colis> findByDateFinAfter(LocalDateTime date);

    @Query("SELECT c FROM Colis c WHERE c.poid BETWEEN :poidMin AND :poidMax")
    List<Colis> findByPoidRange(@Param("poidMin") BigDecimal poidMin, @Param("poidMax") BigDecimal poidMax);

    @Query("SELECT c FROM Colis c WHERE c.taille BETWEEN :tailleMin AND :tailleMax")
    List<Colis> findByTailleRange(@Param("tailleMin") Integer tailleMin, @Param("tailleMax") Integer tailleMax);

    @Query("SELECT c FROM Colis c WHERE c.dateDebut <= :now AND c.dateFin >= :now")
    List<Colis> findActivePackages(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Colis c WHERE c.type = :type")
    long countColisByType(@Param("type") String type);
}