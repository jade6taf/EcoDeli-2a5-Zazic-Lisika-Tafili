package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.models.Annonce.TypeAnnonce;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Integer> {

    List<Annonce> findByTypeAnnonce(TypeAnnonce typeAnnonce);

    List<Annonce> findByStatut(StatutAnnonce statut);

    List<Annonce> findByExpediteur(Utilisateur expediteur);

    List<Annonce> findByExpediteurIdUtilisateur(Integer idExpediteur);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'active' AND a.adresseDepart LIKE %:ville%")
    List<Annonce> findActiveAnnoncesByDepartureCity(@Param("ville") String ville);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'active' AND a.adresseFin LIKE %:ville%")
    List<Annonce> findActiveAnnoncesByArrivalCity(@Param("ville") String ville);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'active' AND a.prixUnitaire BETWEEN :min AND :max")
    List<Annonce> findActiveAnnoncesByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

}