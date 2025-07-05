package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.EscrowTransaction;
import com.ecodeli.ecodeli_backend.models.EscrowTransaction.EscrowStatus;
import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Client;
import com.ecodeli.ecodeli_backend.models.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, Long> {

    Optional<EscrowTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<EscrowTransaction> findByAnnonceOrderByDateCreationDesc(Annonce annonce);

    List<EscrowTransaction> findByClientOrderByDateCreationDesc(Client client);

    List<EscrowTransaction> findByLivreurOrderByDateCreationDesc(Livreur livreur);

    List<EscrowTransaction> findByStatutOrderByDateCreationDesc(EscrowStatus statut);

    @Query("SELECT e FROM EscrowTransaction e WHERE e.annonce.idAnnonce = :annonceId AND e.segmentNumero = :segmentNumero")
    Optional<EscrowTransaction> findByAnnonceIdAndSegmentNumero(@Param("annonceId") Integer annonceId, @Param("segmentNumero") Integer segmentNumero);

    @Query("SELECT e FROM EscrowTransaction e WHERE e.livreur.idUtilisateur = :livreurId AND e.statut = :statut")
    List<EscrowTransaction> findByLivreurIdAndStatut(@Param("livreurId") Integer livreurId, @Param("statut") EscrowStatus statut);

    @Query("SELECT COUNT(e) FROM EscrowTransaction e WHERE e.statut = :statut")
    Long countByStatut(@Param("statut") EscrowStatus statut);

    @Query("SELECT SUM(e.commissionEcodeli) FROM EscrowTransaction e WHERE e.statut = 'RELEASED'")
    Double getTotalCommissionEcodeli();

    @Query("SELECT e FROM EscrowTransaction e WHERE e.annonce.idAnnonce = :annonceId ORDER BY e.segmentNumero ASC")
    List<EscrowTransaction> findAllByAnnonceIdOrderBySegment(@Param("annonceId") Integer annonceId);
}
