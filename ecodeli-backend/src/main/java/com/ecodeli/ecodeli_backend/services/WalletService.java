package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletLivreurRepository walletRepository;
    private final TransactionWalletRepository transactionRepository;
    private final UtilisateurService utilisateurService;

    @Transactional
    public WalletLivreur getOrCreateWallet(Integer livreurId) {
        Livreur livreur = (Livreur) utilisateurService.getUtilisateurById(livreurId)
            .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        return walletRepository.findByLivreur(livreur)
            .orElseGet(() -> {
                WalletLivreur wallet = new WalletLivreur();
                wallet.setLivreur(livreur);
                wallet.setSoldeDisponible(BigDecimal.ZERO);
                wallet.setSoldeEnAttente(BigDecimal.ZERO);
                return walletRepository.save(wallet);
            });
    }

    public Optional<WalletLivreur> getWalletByLivreurId(Integer livreurId) {
        return walletRepository.findByLivreurId(livreurId);
    }

    @Transactional
    public WalletLivreur updateBankInfo(Integer livreurId, String iban, String nomTitulaire) {
        WalletLivreur wallet = getOrCreateWallet(livreurId);

        if (iban == null || iban.trim().length() < 15) {
            throw new IllegalArgumentException("IBAN invalide");
        }

        if (nomTitulaire == null || nomTitulaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom du titulaire requis");
        }

        wallet.setIban(iban.trim().toUpperCase());
        wallet.setNomTitulaireCompte(nomTitulaire.trim());
        wallet.setDateDerniereMaj(LocalDateTime.now());

        return walletRepository.save(wallet);
    }

    @Transactional
    public WalletLivreur crediterWallet(Integer livreurId, BigDecimal montant, String description, 
                                       TransactionWallet.TypeTransaction type, Livraison livraison) {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        WalletLivreur wallet = getOrCreateWallet(livreurId);

        TransactionWallet transaction = new TransactionWallet();
        transaction.setWallet(wallet);
        transaction.setMontant(montant);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setLivraison(livraison);
        transaction.setStatut(TransactionWallet.StatutTransaction.COMPLETED);
        transaction.setDateTransaction(LocalDateTime.now());

        wallet.setSoldeDisponible(wallet.getSoldeDisponible().add(montant));
        wallet.setDateDerniereMaj(LocalDateTime.now());

        transactionRepository.save(transaction);
        WalletLivreur savedWallet = walletRepository.save(wallet);

        log.info("Wallet {} crédité de {}€ - Nouveau solde: {}€",
                livreurId, montant, savedWallet.getSoldeDisponible());

        return savedWallet;
    }

    @Transactional
    public WalletLivreur debiterWallet(Integer livreurId, BigDecimal montant, String description,
                                      TransactionWallet.TypeTransaction type) {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        WalletLivreur wallet = getOrCreateWallet(livreurId);
        if (wallet.getSoldeDisponible().compareTo(montant) < 0) {
            throw new RuntimeException("Solde insuffisant");
        }

        TransactionWallet transaction = new TransactionWallet();
        transaction.setWallet(wallet);
        transaction.setMontant(montant.negate());
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setStatut(TransactionWallet.StatutTransaction.COMPLETED);
        transaction.setDateTransaction(LocalDateTime.now());

        wallet.setSoldeDisponible(wallet.getSoldeDisponible().subtract(montant));
        wallet.setDateDerniereMaj(LocalDateTime.now());

        transactionRepository.save(transaction);
        WalletLivreur savedWallet = walletRepository.save(wallet);

        log.info("Wallet {} débité de {}€ - Nouveau solde: {}€",
                livreurId, montant, savedWallet.getSoldeDisponible());

        return savedWallet;
    }

    public List<TransactionWallet> getTransactionHistory(Integer livreurId) {
        return transactionRepository.findByLivreurIdOrderByDateDesc(livreurId);
    }

    public Page<TransactionWallet> getTransactionHistory(Integer livreurId, Pageable pageable) {
        return transactionRepository.findByLivreurIdOrderByDateDesc(livreurId, pageable);
    }

    public List<TransactionWallet> getTransactionsByType(Integer livreurId, TransactionWallet.TypeTransaction type) {
        WalletLivreur wallet = getOrCreateWallet(livreurId);
        return transactionRepository.findByWalletAndTypeOrderByDateTransactionDesc(wallet, type);
    }

    public WalletStats getWalletStats(Integer livreurId) {
        WalletLivreur wallet = getOrCreateWallet(livreurId);
        Double totalGains = transactionRepository.getSumByWalletAndType(
            wallet, TransactionWallet.TypeTransaction.CREDIT_LIVRAISON);
        Double totalRetraits = transactionRepository.getSumByWalletAndType(
            wallet, TransactionWallet.TypeTransaction.DEBIT_RETRAIT);
        Long nombreLivraisons = transactionRepository.countLivraisonsCompletedByWallet(wallet);
        return WalletStats.builder()
            .soldeDisponible(wallet.getSoldeDisponible())
            .soldeEnAttente(wallet.getSoldeEnAttente())
            .totalGains(totalGains != null ? BigDecimal.valueOf(totalGains) : BigDecimal.ZERO)
            .totalRetraits(totalRetraits != null ? BigDecimal.valueOf(Math.abs(totalRetraits)) : BigDecimal.ZERO)
            .nombreLivraisons(nombreLivraisons != null ? nombreLivraisons : 0L)
            .ibanConfigured(wallet.hasValidIban())
            .build();
    }

    public boolean canWithdraw(Integer livreurId, BigDecimal montant) {
        Optional<WalletLivreur> walletOpt = getWalletByLivreurId(livreurId);
        if (walletOpt.isEmpty()) {
            return false;
        }
        WalletLivreur wallet = walletOpt.get();
        return wallet.peutRetirerMontant(montant) && wallet.hasValidIban();
    }

    public List<WalletLivreur> getWalletsWithPositiveBalance() {
        return walletRepository.findAllWithPositiveBalance();
    }

    public GlobalWalletStats getGlobalStats() {
        Double totalDisponible = walletRepository.getTotalSoldeDisponible();
        Double totalEnAttente = walletRepository.getTotalSoldeEnAttente();
        Long walletsEligibles = walletRepository.countWalletsEligibleForWithdrawal();
        return GlobalWalletStats.builder()
            .totalSoldeDisponible(totalDisponible != null ? BigDecimal.valueOf(totalDisponible) : BigDecimal.ZERO)
            .totalSoldeEnAttente(totalEnAttente != null ? BigDecimal.valueOf(totalEnAttente) : BigDecimal.ZERO)
            .nombreWalletsEligiblesRetrait(walletsEligibles != null ? walletsEligibles : 0L)
            .build();
    }

    public static class WalletStats {
        private BigDecimal soldeDisponible;
        private BigDecimal soldeEnAttente;
        private BigDecimal totalGains;
        private BigDecimal totalRetraits;
        private Long nombreLivraisons;
        private boolean ibanConfigured;

        public static WalletStatsBuilder builder() {
            return new WalletStatsBuilder();
        }

        public static class WalletStatsBuilder {
            private BigDecimal soldeDisponible;
            private BigDecimal soldeEnAttente;
            private BigDecimal totalGains;
            private BigDecimal totalRetraits;
            private Long nombreLivraisons;
            private boolean ibanConfigured;

            public WalletStatsBuilder soldeDisponible(BigDecimal soldeDisponible) {
                this.soldeDisponible = soldeDisponible;
                return this;
            }

            public WalletStatsBuilder soldeEnAttente(BigDecimal soldeEnAttente) {
                this.soldeEnAttente = soldeEnAttente;
                return this;
            }

            public WalletStatsBuilder totalGains(BigDecimal totalGains) {
                this.totalGains = totalGains;
                return this;
            }

            public WalletStatsBuilder totalRetraits(BigDecimal totalRetraits) {
                this.totalRetraits = totalRetraits;
                return this;
            }

            public WalletStatsBuilder nombreLivraisons(Long nombreLivraisons) {
                this.nombreLivraisons = nombreLivraisons;
                return this;
            }

            public WalletStatsBuilder ibanConfigured(boolean ibanConfigured) {
                this.ibanConfigured = ibanConfigured;
                return this;
            }

            public WalletStats build() {
                WalletStats stats = new WalletStats();
                stats.soldeDisponible = this.soldeDisponible;
                stats.soldeEnAttente = this.soldeEnAttente;
                stats.totalGains = this.totalGains;
                stats.totalRetraits = this.totalRetraits;
                stats.nombreLivraisons = this.nombreLivraisons;
                stats.ibanConfigured = this.ibanConfigured;
                return stats;
            }
        }

        public BigDecimal getSoldeDisponible() { return soldeDisponible; }
        public BigDecimal getSoldeEnAttente() { return soldeEnAttente; }
        public BigDecimal getTotalGains() { return totalGains; }
        public BigDecimal getTotalRetraits() { return totalRetraits; }
        public Long getNombreLivraisons() { return nombreLivraisons; }
        public boolean isIbanConfigured() { return ibanConfigured; }
    }

    public static class GlobalWalletStats {
        private BigDecimal totalSoldeDisponible;
        private BigDecimal totalSoldeEnAttente;
        private Long nombreWalletsEligiblesRetrait;

        public static GlobalWalletStatsBuilder builder() {
            return new GlobalWalletStatsBuilder();
        }

        public static class GlobalWalletStatsBuilder {
            private BigDecimal totalSoldeDisponible;
            private BigDecimal totalSoldeEnAttente;
            private Long nombreWalletsEligiblesRetrait;

            public GlobalWalletStatsBuilder totalSoldeDisponible(BigDecimal totalSoldeDisponible) {
                this.totalSoldeDisponible = totalSoldeDisponible;
                return this;
            }

            public GlobalWalletStatsBuilder totalSoldeEnAttente(BigDecimal totalSoldeEnAttente) {
                this.totalSoldeEnAttente = totalSoldeEnAttente;
                return this;
            }

            public GlobalWalletStatsBuilder nombreWalletsEligiblesRetrait(Long nombreWalletsEligiblesRetrait) {
                this.nombreWalletsEligiblesRetrait = nombreWalletsEligiblesRetrait;
                return this;
            }

            public GlobalWalletStats build() {
                GlobalWalletStats stats = new GlobalWalletStats();
                stats.totalSoldeDisponible = this.totalSoldeDisponible;
                stats.totalSoldeEnAttente = this.totalSoldeEnAttente;
                stats.nombreWalletsEligiblesRetrait = this.nombreWalletsEligiblesRetrait;
                return stats;
            }
        }

        public BigDecimal getTotalSoldeDisponible() { return totalSoldeDisponible; }
        public BigDecimal getTotalSoldeEnAttente() { return totalSoldeEnAttente; }
        public Long getNombreWalletsEligiblesRetrait() { return nombreWalletsEligiblesRetrait; }
    }
}
