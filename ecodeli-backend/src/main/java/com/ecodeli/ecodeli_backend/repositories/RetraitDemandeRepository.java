package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.RetraitDemande;
import com.ecodeli.ecodeli_backend.models.RetraitDemande.StatutRetrait;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.WalletLivreur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RetraitDemandeRepository extends JpaRepository<RetraitDemande, Long> {
    List<RetraitDemande> findByLivreurOrderByDateDemandeDesc(Livreur livreur);

    Page<RetraitDemande> findByLivreurOrderByDateDemandeDesc(Livreur livreur, Pageable pageable);

    @Query("SELECT r FROM RetraitDemande r WHERE r.livreur.idUtilisateur = :livreurId ORDER BY r.dateDemande DESC")
    List<RetraitDemande> findByLivreurIdOrderByDateDesc(@Param("livreurId") Integer livreurId);

    @Query("SELECT r FROM RetraitDemande r WHERE r.livreur.idUtilisateur = :livreurId ORDER BY r.dateDemande DESC")
    Page<RetraitDemande> findByLivreurIdOrderByDateDesc(@Param("livreurId") Integer livreurId, Pageable pageable);

    List<RetraitDemande> findByWalletOrderByDateDemandeDesc(WalletLivreur wallet);

    List<RetraitDemande> findByStatutOrderByDateDemandeDesc(StatutRetrait statut);

    Page<RetraitDemande> findByStatutOrderByDateDemandeDesc(StatutRetrait statut, Pageable pageable);

    @Query("SELECT r FROM RetraitDemande r WHERE r.statut IN ('PENDING', 'PROCESSING') ORDER BY r.dateDemande ASC")
    List<RetraitDemande> findAllPendingRetraits();

    @Query("SELECT r FROM RetraitDemande r WHERE r.dateDemande BETWEEN :debut AND :fin ORDER BY r.dateDemande DESC")
    List<RetraitDemande> findByDateDemandeBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT r FROM RetraitDemande r WHERE r.livreur = :livreur AND r.statut = :statut ORDER BY r.dateDemande DESC")
    List<RetraitDemande> findByLivreurAndStatut(@Param("livreur") Livreur livreur, @Param("statut") StatutRetrait statut);

    @Query("SELECT COUNT(r) FROM RetraitDemande r WHERE r.statut = :statut")
    Long countByStatut(@Param("statut") StatutRetrait statut);

    @Query("SELECT SUM(r.montant) FROM RetraitDemande r WHERE r.statut = 'COMPLETED'")
    Double getTotalMontantCompleted();

    @Query("SELECT SUM(r.montant) FROM RetraitDemande r WHERE r.statut = 'PENDING'")
    Double getTotalMontantPending();

    @Query("SELECT r FROM RetraitDemande r WHERE r.livreur = :livreur AND r.statut = 'PENDING'")
    List<RetraitDemande> findPendingRetraitsByLivreur(@Param("livreur") Livreur livreur);

    @Query("SELECT COUNT(r) FROM RetraitDemande r WHERE r.livreur = :livreur AND r.statut = 'COMPLETED'")
    Long countCompletedRetraitsByLivreur(@Param("livreur") Livreur livreur);
}
