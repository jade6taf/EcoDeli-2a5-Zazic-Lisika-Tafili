package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.EscrowTransaction;
import com.ecodeli.ecodeli_backend.services.PaymentService;
import com.ecodeli.ecodeli_backend.services.PaymentService.PaymentIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            PaymentIntentResponse response = paymentService.createPaymentIntent(
                request.getAnnonceId(),
                request.getClientId(),
                request.getMontant()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création du paiement"));
        }
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        try {
            EscrowTransaction escrowTransaction = paymentService.confirmPayment(request.getPaymentIntentId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "escrowId", escrowTransaction.getIdEscrow(),
                "status", escrowTransaction.getStatut(),
                "message", "Paiement confirmé et fonds bloqués en sécurité"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> refundPayment(@Valid @RequestBody RefundPaymentRequest request) {
        try {
            paymentService.refundPayment(request.getPaymentIntentId(), request.getReason());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Remboursement effectué avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/release-funds")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('ADMIN')")
    public ResponseEntity<?> releaseFunds(@Valid @RequestBody ReleaseFundsRequest request) {
        try {
            paymentService.releaseFundsToDelivery(
                request.getAnnonceId(),
                request.getLivreurId(),
                request.getSegmentNumero()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fonds libérés vers le portefeuille du livreur"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/annonce/{annonceId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('LIVREUR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentInfo(@PathVariable Integer annonceId) {
        try {
            return ResponseEntity.ok(Map.of(
                "annonceId", annonceId,
                "message", "Informations de paiement - À implémenter"
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des informations de paiement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    public static class CreatePaymentIntentRequest {
        @NotNull(message = "L'ID de l'annonce est requis")
        private Integer annonceId;

        @NotNull(message = "L'ID du client est requis")
        private Integer clientId;

        @NotNull(message = "Le montant est requis")
        @Positive(message = "Le montant doit être positif")
        private BigDecimal montant;

        public Integer getAnnonceId() { return annonceId; }
        public void setAnnonceId(Integer annonceId) { this.annonceId = annonceId; }

        public Integer getClientId() { return clientId; }
        public void setClientId(Integer clientId) { this.clientId = clientId; }

        public BigDecimal getMontant() { return montant; }
        public void setMontant(BigDecimal montant) { this.montant = montant; }
    }

    public static class ConfirmPaymentRequest {
        @NotNull(message = "L'ID du Payment Intent est requis")
        private String paymentIntentId;

        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    }

    public static class RefundPaymentRequest {
        @NotNull(message = "L'ID du Payment Intent est requis")
        private String paymentIntentId;

        private String reason = "Annulation de commande";

        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ReleaseFundsRequest {
        @NotNull(message = "L'ID de l'annonce est requis")
        private Integer annonceId;

        @NotNull(message = "L'ID du livreur est requis")
        private Integer livreurId;

        private Integer segmentNumero;

        public Integer getAnnonceId() { return annonceId; }
        public void setAnnonceId(Integer annonceId) { this.annonceId = annonceId; }

        public Integer getLivreurId() { return livreurId; }
        public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }

        public Integer getSegmentNumero() { return segmentNumero; }
        public void setSegmentNumero(Integer segmentNumero) { this.segmentNumero = segmentNumero; }
    }
}
