package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.TransactionWallet;
import com.ecodeli.ecodeli_backend.models.TransactionWallet.TypeTransaction;
import com.ecodeli.ecodeli_backend.models.TransactionWallet.StatutTransaction;
import com.ecodeli.ecodeli_backend.models.WalletLivreur;
import com.ecodeli.ecodeli_backend.models.Livraison;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionWalletRepository extends JpaRepository<TransactionWallet, Long> {
    List<TransactionWallet> findByWalletOrderByDateTransactionDesc(WalletLivreur wallet);

    Page<TransactionWallet> findByWalletOrderByDateTransactionDesc(WalletLivreur wallet, Pageable pageable);

    @Query("SELECT t FROM TransactionWallet t WHERE t.wallet.livreur.idUtilisateur = :livreurId ORDER BY t.dateTransaction DESC")
    List<TransactionWallet> findByLivreurIdOrderByDateDesc(@Param("livreurId") Integer livreurId);

    @Query("SELECT t FROM TransactionWallet t WHERE t.wallet.livreur.idUtilisateur = :livreurId ORDER BY t.dateTransaction DESC")
    Page<TransactionWallet> findByLivreurIdOrderByDateDesc(@Param("livreurId") Integer livreurId, Pageable pageable);

    List<TransactionWallet> findByWalletAndTypeOrderByDateTransactionDesc(WalletLivreur wallet, TypeTransaction type);

    List<TransactionWallet> findByWalletAndStatutOrderByDateTransactionDesc(WalletLivreur wallet, StatutTransaction statut);

    List<TransactionWallet> findByLivraisonOrderByDateTransactionDesc(Livraison livraison);

    @Query("SELECT t FROM TransactionWallet t WHERE t.dateTransaction BETWEEN :debut AND :fin ORDER BY t.dateTransaction DESC")
    List<TransactionWallet> findByDateTransactionBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT t FROM TransactionWallet t WHERE t.wallet = :wallet AND t.dateTransaction BETWEEN :debut AND :fin ORDER BY t.dateTransaction DESC")
    List<TransactionWallet> findByWalletAndDateTransactionBetween(@Param("wallet") WalletLivreur wallet, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(t.montant) FROM TransactionWallet t WHERE t.wallet = :wallet AND t.type = :type AND t.statut = 'COMPLETED'")
    Double getSumByWalletAndType(@Param("wallet") WalletLivreur wallet, @Param("type") TypeTransaction type);

    @Query("SELECT COUNT(t) FROM TransactionWallet t WHERE t.wallet = :wallet AND t.type = 'CREDIT_LIVRAISON'")
    Long countLivraisonsCompletedByWallet(@Param("wallet") WalletLivreur wallet);
}
