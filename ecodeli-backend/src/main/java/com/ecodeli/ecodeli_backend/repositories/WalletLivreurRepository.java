package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.WalletLivreur;
import com.ecodeli.ecodeli_backend.models.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletLivreurRepository extends JpaRepository<WalletLivreur, Long> {
    Optional<WalletLivreur> findByLivreur(Livreur livreur);

    @Query("SELECT w FROM WalletLivreur w WHERE w.livreur.idUtilisateur = :livreurId")
    Optional<WalletLivreur> findByLivreurId(@Param("livreurId") Integer livreurId);

    @Query("SELECT w FROM WalletLivreur w WHERE w.soldeDisponible >= :montantMinimum")
    List<WalletLivreur> findAllWithSoldeMinimum(@Param("montantMinimum") BigDecimal montantMinimum);

    @Query("SELECT w FROM WalletLivreur w WHERE w.soldeDisponible > 0 ORDER BY w.soldeDisponible DESC")
    List<WalletLivreur> findAllWithPositiveBalance();

    @Query("SELECT SUM(w.soldeDisponible) FROM WalletLivreur w")
    Double getTotalSoldeDisponible();

    @Query("SELECT SUM(w.soldeEnAttente) FROM WalletLivreur w")
    Double getTotalSoldeEnAttente();

    @Query("SELECT COUNT(w) FROM WalletLivreur w WHERE w.soldeDisponible >= 10.0")
    Long countWalletsEligibleForWithdrawal();

    @Query("SELECT w FROM WalletLivreur w WHERE w.iban IS NOT NULL AND w.nomTitulaireCompte IS NOT NULL")
    List<WalletLivreur> findAllWithValidIban();

    @Query("SELECT w FROM WalletLivreur w WHERE w.iban IS NULL OR w.nomTitulaireCompte IS NULL")
    List<WalletLivreur> findAllWithoutValidIban();
}
