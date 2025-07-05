package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService {

    private final RetraitDemandeRepository retraitRepository;
    private final WalletService walletService;
    private final UtilisateurService utilisateurService;
    private final EmailService emailService;

    @Transactional
    public RetraitDemande requestWithdrawal(Integer livreurId, BigDecimal montant, String iban, String nomTitulaire) {
        if (montant.compareTo(BigDecimal.valueOf(1)) < 0) {
            throw new IllegalArgumentException("Le montant minimum de retrait est de 1€");
        }

        Livreur livreur = (Livreur) utilisateurService.getUtilisateurById(livreurId)
            .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        WalletLivreur wallet = walletService.getOrCreateWallet(livreurId);

        if (wallet.getSoldeDisponible().compareTo(montant) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer ce retrait");
        }

        RetraitDemande retrait = new RetraitDemande();
        retrait.setLivreur(livreur);
        retrait.setWallet(wallet);
        retrait.setMontant(montant);
        retrait.setIban(iban.trim().toUpperCase());
        retrait.setNomTitulaire(nomTitulaire.trim());
        retrait.setStatut(RetraitDemande.StatutRetrait.PENDING);
        retrait.setDateDemande(LocalDateTime.now());

        retrait = retraitRepository.save(retrait);

        try {
            walletService.debiterWallet(
                livreurId,
                montant,
                "Retrait fictif vers " + iban.substring(Math.max(0, iban.length() - 4)),
                TransactionWallet.TypeTransaction.DEBIT_RETRAIT_FICTIF
            );

            String transferId = "FICTIF_" + System.currentTimeMillis() + "_" + retrait.getIdRetrait();
            retrait.marquerCommeTraite(LocalDateTime.now(), transferId);
            retrait.setIsFictive(true);
            retrait.setTypeRetrait(RetraitDemande.TypeRetrait.FICTIF);
            retrait = retraitRepository.save(retrait);

            log.info("Retrait fictif traité automatiquement: {} pour {}€", retrait.getIdRetrait(), montant);

            try {
                if (livreur.getEmail() != null) {
                    emailService.sendFictiveWithdrawalConfirmation(retrait);
                }
            } catch (Exception e) {
                log.warn("Erreur lors de l'envoi de l'email de retrait fictif: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Erreur lors du traitement automatique du retrait fictif", e);
            try {
                if (livreur.getEmail() != null) {
                    emailService.sendWithdrawalRequestConfirmation(retrait);
                }
            } catch (Exception emailException) {
                log.warn("Erreur lors de l'envoi de l'email de confirmation: {}", emailException.getMessage());
            }
        }

        log.info("Demande de retrait créée: {} pour {}€", retrait.getIdRetrait(), montant);
        return retrait;
    }

    public Page<RetraitDemande> getWithdrawalHistory(Integer livreurId, Pageable pageable) {
        return retraitRepository.findByLivreurIdOrderByDateDesc(livreurId, pageable);
    }

    public Map<String, Object> canWithdraw(Integer livreurId, BigDecimal montant) {
        Map<String, Object> result = new HashMap<>();
        try {
            WalletLivreur wallet = walletService.getOrCreateWallet(livreurId);

            boolean hasValidIban = wallet.hasValidIban();
            boolean hasSufficientBalance = wallet.getSoldeDisponible().compareTo(montant) >= 0;
            boolean aboveMinimum = montant.compareTo(BigDecimal.valueOf(1)) >= 0;

            boolean canWithdraw = hasValidIban && hasSufficientBalance && aboveMinimum;
            result.put("canWithdraw", canWithdraw);
            result.put("hasValidIban", hasValidIban);
            result.put("hasSufficientBalance", hasSufficientBalance);
            result.put("aboveMinimum", aboveMinimum);
            result.put("noPendingWithdrawals", true); // ✅ Toujours vrai pour retraits directs
            result.put("soldeDisponible", wallet.getSoldeDisponible());
            result.put("montantRequis", montant);
            result.put("montantMinimum", BigDecimal.valueOf(1));
        } catch (Exception e) {
            result.put("canWithdraw", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getWithdrawalStats() {
        Map<String, Object> stats = new HashMap<>();

        for (RetraitDemande.StatutRetrait statut : RetraitDemande.StatutRetrait.values()) {
            Long count = retraitRepository.countByStatut(statut);
            stats.put("count" + statut.name(), count != null ? count : 0L);
        }

        Double totalCompleted = retraitRepository.getTotalMontantCompleted();
        Double totalPending = retraitRepository.getTotalMontantPending();
        stats.put("totalMontantCompleted", totalCompleted != null ? BigDecimal.valueOf(totalCompleted) : BigDecimal.ZERO);
        stats.put("totalMontantPending", totalPending != null ? BigDecimal.valueOf(totalPending) : BigDecimal.ZERO);
        return stats;
    }
}
