package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final EscrowTransactionRepository escrowRepository;
    private final WalletLivreurRepository walletRepository;
    private final UtilisateurService utilisateurService;
    private final AnnonceRepository annonceRepository;

    @Transactional
    public PaymentIntentResponse createPaymentIntent(Integer annonceId, Integer clientId, BigDecimal montant) {
        try {
            if (montant.compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new IllegalArgumentException("Le montant minimum est de 1€");
            }

            Client client = (Client) utilisateurService.getUtilisateurById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

            Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

            if (!annonce.getExpediteur().getIdUtilisateur().equals(clientId)) {
                throw new RuntimeException("Cette annonce ne vous appartient pas");
            }

            long montantCentimes = montant.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .addPaymentMethodType("card")
                .setDescription("Paiement pour annonce #" + annonceId)
                .putMetadata("annonce_id", annonceId.toString())
                .putMetadata("client_id", clientId.toString())
                .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            EscrowTransaction escrowTransaction = new EscrowTransaction();
            escrowTransaction.setStripePaymentIntentId(paymentIntent.getId());
            escrowTransaction.setAnnonce(annonce);
            escrowTransaction.setClient(client);
            escrowTransaction.setMontantTotal(montant);
            escrowTransaction.calculerMontants();
            escrowTransaction.setStatut(EscrowTransaction.EscrowStatus.PENDING);

            escrowRepository.save(escrowTransaction);

            log.info("Payment Intent créé: {} pour montant: {}€", paymentIntent.getId(), montant);

            return PaymentIntentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .paymentIntentId(paymentIntent.getId())
                .montant(montant)
                .commission(escrowTransaction.getCommissionEcodeli())
                .montantLivreur(escrowTransaction.getMontantLivreur())
                .build();

        } catch (StripeException e) {
            log.error("Erreur lors de la création du Payment Intent", e);
            throw new RuntimeException("Erreur de paiement: " + e.getMessage());
        }
    }

    @Transactional
    public EscrowTransaction confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            EscrowTransaction escrowTransaction = escrowRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Transaction escrow non trouvée"));

            if ("succeeded".equals(paymentIntent.getStatus())) {
                escrowTransaction.setStatut(EscrowTransaction.EscrowStatus.HELD);
                escrowTransaction = escrowRepository.save(escrowTransaction);
                log.info("Paiement confirmé et fonds bloqués en escrow: {}", paymentIntentId);
                return escrowTransaction;
            } else {
                escrowTransaction.setStatut(EscrowTransaction.EscrowStatus.FAILED);
                escrowRepository.save(escrowTransaction);
                throw new RuntimeException("Le paiement a échoué: " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Erreur lors de la confirmation du paiement", e);
            throw new RuntimeException("Erreur de confirmation: " + e.getMessage());
        }
    }

    @Transactional
    public void releaseFundsToDelivery(Integer annonceId, Integer livreurId, Integer segmentNumero) {
        try {
            log.info("Tentative de libération des fonds pour annonce {}, livreur {}, segment {}", annonceId, livreurId, segmentNumero);
            List<EscrowTransaction> allTransactions = escrowRepository.findAllByAnnonceIdOrderBySegment(annonceId);

            log.info("Transactions escrow trouvées: {}", allTransactions.size());
            for (EscrowTransaction tx : allTransactions) {
                log.info("Transaction: ID={}, Statut={}, Segment={}, Montant={}",
                    tx.getIdEscrow(), tx.getStatut(), tx.getSegmentNumero(), tx.getMontantTotal());
            }

            EscrowTransaction escrowTransaction;
            if (segmentNumero == null) {
                escrowTransaction = allTransactions.stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucune transaction escrow trouvée pour l'annonce " + annonceId));
            } else {
                escrowTransaction = escrowRepository.findByAnnonceIdAndSegmentNumero(annonceId, segmentNumero)
                    .orElseThrow(() -> new RuntimeException("Transaction escrow non trouvée pour l'annonce " + annonceId + " segment " + segmentNumero));
            }
            log.info("Transaction escrow sélectionnée: Statut={}, Montant={}", escrowTransaction.getStatut(), escrowTransaction.getMontantTotal());
            if (escrowTransaction.getStatut() != EscrowTransaction.EscrowStatus.HELD &&
                escrowTransaction.getStatut() != EscrowTransaction.EscrowStatus.PENDING) {
                throw new RuntimeException("Les fonds ne sont pas en état d'être libérés. Statut actuel: " + escrowTransaction.getStatut());
            }

            Livreur livreur = (Livreur) utilisateurService.getUtilisateurById(livreurId)
                .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

            WalletLivreur wallet = walletRepository.findByLivreur(livreur)
                .orElseGet(() -> {
                    WalletLivreur newWallet = new WalletLivreur();
                    newWallet.setLivreur(livreur);
                    return walletRepository.save(newWallet);
                });

            log.info("Wallet livreur trouvé/créé. Solde actuel: {}", wallet.getSoldeDisponible());

            wallet.ajouterFonds(
                escrowTransaction.getMontantLivreur(),
                "Paiement livraison #" + annonceId + (segmentNumero != null ? " - Segment " + segmentNumero : ""),
                TransactionWallet.TypeTransaction.CREDIT_LIVRAISON
            );

            escrowTransaction.setStatut(EscrowTransaction.EscrowStatus.RELEASED);
            escrowTransaction.setLivreur(livreur);
            escrowTransaction.setDateLiberation(LocalDateTime.now());

            walletRepository.save(wallet);
            escrowRepository.save(escrowTransaction);

            log.info("Fonds libérés avec succès vers le wallet du livreur {} pour l'annonce {}. Nouveau solde: {}", 
                livreurId, annonceId, wallet.getSoldeDisponible());

        } catch (Exception e) {
            log.error("Erreur lors de la libération des fonds pour annonce {}, livreur {}: {}", annonceId, livreurId, e.getMessage(), e);
            throw new RuntimeException("Erreur de libération: " + e.getMessage());
        }
    }

    @Transactional
    public void refundPayment(String paymentIntentId, String reason) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("payment_intent", paymentIntentId);
            refundParams.put("reason", "requested_by_customer");

            com.stripe.model.Refund refund = com.stripe.model.Refund.create(refundParams);

            EscrowTransaction escrowTransaction = escrowRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Transaction escrow non trouvée"));

            escrowTransaction.setStatut(EscrowTransaction.EscrowStatus.REFUNDED);
            escrowTransaction.setNotes(reason);
            escrowRepository.save(escrowTransaction);

            log.info("Remboursement effectué: {} - Raison: {}", refund.getId(), reason);

        } catch (StripeException e) {
            log.error("Erreur lors du remboursement", e);
            throw new RuntimeException("Erreur de remboursement: " + e.getMessage());
        }
    }

    public static class PaymentIntentResponse {
        private String clientSecret;
        private String paymentIntentId;
        private BigDecimal montant;
        private BigDecimal commission;
        private BigDecimal montantLivreur;

        public static PaymentIntentResponseBuilder builder() {
            return new PaymentIntentResponseBuilder();
        }

        public static class PaymentIntentResponseBuilder {
            private String clientSecret;
            private String paymentIntentId;
            private BigDecimal montant;
            private BigDecimal commission;
            private BigDecimal montantLivreur;

            public PaymentIntentResponseBuilder clientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
                return this;
            }

            public PaymentIntentResponseBuilder paymentIntentId(String paymentIntentId) {
                this.paymentIntentId = paymentIntentId;
                return this;
            }

            public PaymentIntentResponseBuilder montant(BigDecimal montant) {
                this.montant = montant;
                return this;
            }

            public PaymentIntentResponseBuilder commission(BigDecimal commission) {
                this.commission = commission;
                return this;
            }

            public PaymentIntentResponseBuilder montantLivreur(BigDecimal montantLivreur) {
                this.montantLivreur = montantLivreur;
                return this;
            }

            public PaymentIntentResponse build() {
                PaymentIntentResponse response = new PaymentIntentResponse();
                response.clientSecret = this.clientSecret;
                response.paymentIntentId = this.paymentIntentId;
                response.montant = this.montant;
                response.commission = this.commission;
                response.montantLivreur = this.montantLivreur;
                return response;
            }
        }

        public String getClientSecret() { return clientSecret; }
        public String getPaymentIntentId() { return paymentIntentId; }
        public BigDecimal getMontant() { return montant; }
        public BigDecimal getCommission() { return commission; }
        public BigDecimal getMontantLivreur() { return montantLivreur; }
    }
}
