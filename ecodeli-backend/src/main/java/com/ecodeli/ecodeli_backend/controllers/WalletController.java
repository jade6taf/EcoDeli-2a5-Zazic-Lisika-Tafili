package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.WalletLivreur;
import com.ecodeli.ecodeli_backend.models.TransactionWallet;
import com.ecodeli.ecodeli_backend.services.WalletService;
import com.ecodeli.ecodeli_backend.services.WalletService.WalletStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance/{livreurId}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getWalletBalance(@PathVariable Integer livreurId) {
        try {
            Optional<WalletLivreur> walletOpt = walletService.getWalletByLivreurId(livreurId);
            if (walletOpt.isEmpty()) {
                WalletLivreur newWallet = walletService.getOrCreateWallet(livreurId);
                return ResponseEntity.ok(createWalletResponse(newWallet));
            }
            return ResponseEntity.ok(createWalletResponse(walletOpt.get()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du solde du wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/bank-info/{livreurId}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateBankInfo(@PathVariable Integer livreurId,
                                           @Valid @RequestBody BankInfoRequest request) {
        try {
            WalletLivreur wallet = walletService.updateBankInfo(
                livreurId,
                request.getIban(),
                request.getNomTitulaire()
            );
            log.info("Informations bancaires mises à jour pour le livreur {}", livreurId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Informations bancaires mises à jour avec succès",
                "wallet", createWalletResponse(wallet)
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation pour les informations bancaires: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des informations bancaires", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/{livreurId}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Integer livreurId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TransactionWallet> transactions = walletService.getTransactionHistory(livreurId, pageable);
            return ResponseEntity.ok(Map.of(
                "transactions", transactions.getContent(),
                "totalElements", transactions.getTotalElements(),
                "totalPages", transactions.getTotalPages(),
                "currentPage", transactions.getNumber(),
                "hasNext", transactions.hasNext(),
                "hasPrevious", transactions.hasPrevious()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique des transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/{livreurId}/type/{type}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionsByType(@PathVariable Integer livreurId,
                                                  @PathVariable TransactionWallet.TypeTransaction type) {
        try {
            List<TransactionWallet> transactions = walletService.getTransactionsByType(livreurId, type);
            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des transactions par type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats/{livreurId}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getWalletStats(@PathVariable Integer livreurId) {
        try {
            WalletStats stats = walletService.getWalletStats(livreurId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques du wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/can-withdraw/{livreurId}")
    @PreAuthorize("hasRole('ROLE_LIVREUR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> canWithdraw(@PathVariable Integer livreurId,
                                        @RequestParam(required = false, defaultValue = "10") String montant) {
        try {
            java.math.BigDecimal montantBigDecimal = new java.math.BigDecimal(montant);
            boolean canWithdraw = walletService.canWithdraw(livreurId, montantBigDecimal);

            return ResponseEntity.ok(Map.of(
                "canWithdraw", canWithdraw,
                "montantRequis", montantBigDecimal,
                "montantMinimum", 10
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la vérification de retrait", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/global-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getGlobalStats() {
        try {
            WalletService.GlobalWalletStats stats = walletService.getGlobalStats();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques globales", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/positive-balance")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getWalletsWithPositiveBalance() {
        try {
            List<WalletLivreur> wallets = walletService.getWalletsWithPositiveBalance();
            return ResponseEntity.ok(wallets.stream()
                .map(this::createWalletResponse)
                .toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des wallets avec solde positif", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> createWalletResponse(WalletLivreur wallet) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("idWallet", wallet.getIdWallet());
        response.put("livreurId", wallet.getLivreur().getIdUtilisateur());
        response.put("livreurNom", wallet.getLivreur().getNom() + " " + wallet.getLivreur().getPrenom());
        response.put("soldeDisponible", wallet.getSoldeDisponible());
        response.put("soldeEnAttente", wallet.getSoldeEnAttente());
        response.put("soldeTotal", wallet.getSoldeTotal());
        response.put("iban", wallet.getIban() != null ? maskIban(wallet.getIban()) : null);
        response.put("nomTitulaire", wallet.getNomTitulaireCompte());
        response.put("ibanConfigured", wallet.hasValidIban());
        response.put("dateCreation", wallet.getDateCreation());
        response.put("dateDerniereMaj", wallet.getDateDerniereMaj());
        return response;
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }

    public static class BankInfoRequest {
        @NotBlank(message = "L'IBAN est requis")
        @Size(min = 8, message = "L'IBAN doit contenir au moins 8 caractères")
        private String iban;

        @NotBlank(message = "Le nom du titulaire est requis")
        @Size(min = 2, max = 100, message = "Le nom du titulaire doit contenir entre 2 et 100 caractères")
        private String nomTitulaire;

        public String getIban() { return iban; }
        public void setIban(String iban) { this.iban = iban; }

        public String getNomTitulaire() { return nomTitulaire; }
        public void setNomTitulaire(String nomTitulaire) { this.nomTitulaire = nomTitulaire; }
    }
}
