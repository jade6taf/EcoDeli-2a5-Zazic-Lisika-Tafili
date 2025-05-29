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

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'PUBLIEE' AND a.adresseDepart LIKE %:ville%")
    List<Annonce> findActiveAnnoncesByDepartureCity(@Param("ville") String ville);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'PUBLIEE' AND a.adresseFin LIKE %:ville%")
    List<Annonce> findActiveAnnoncesByArrivalCity(@Param("ville") String ville);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'PUBLIEE' AND a.prixUnitaire BETWEEN :min AND :max")
    List<Annonce> findActiveAnnoncesByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    List<Annonce> findByLivraisonPartielleAutoriseeAndStatut(Boolean livraisonPartielleAutorisee, StatutAnnonce statut);

    @Query("SELECT a FROM Annonce a WHERE a.livraisonPartielleAutorisee = true AND (a.statut = 'PUBLIEE' OR a.statut = 'SEGMENT_1_PRIS' OR a.statut = 'SEGMENT_2_PRIS') AND (a.livreurSegment1 IS NULL OR a.livreurSegment2 IS NULL)")
    List<Annonce> findAnnoncesAvecSegmentsDisponibles();

    @Query("SELECT a FROM Annonce a WHERE a.livraisonPartielleAutorisee = true AND (a.statut = 'PUBLIEE' OR a.statut = 'SEGMENT_2_PRIS') AND a.livreurSegment1 IS NULL")
    List<Annonce> findAnnoncesAvecSegment1Disponible();

    @Query("SELECT a FROM Annonce a WHERE a.livraisonPartielleAutorisee = true AND (a.statut = 'PUBLIEE' OR a.statut = 'SEGMENT_1_PRIS') AND a.livreurSegment2 IS NULL")
    List<Annonce> findAnnoncesAvecSegment2Disponible();

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'SEGMENTS_COMPLETS' AND a.livreurSegment1 IS NOT NULL AND a.livreurSegment2 IS NOT NULL")
    List<Annonce> findAnnoncesAvecSegmentsComplets();

    @Query("SELECT a FROM Annonce a WHERE (a.livreurSegment1.idUtilisateur = :idLivreur OR a.livreurSegment2.idUtilisateur = :idLivreur) AND a.statut IN ('SEGMENTS_COMPLETS', 'EN_COURS_SEGMENT_1', 'ATTENTE_ENTREPOT', 'EN_COURS_SEGMENT_2')")
    List<Annonce> findMesSegments(@Param("idLivreur") Integer idLivreur);
}
