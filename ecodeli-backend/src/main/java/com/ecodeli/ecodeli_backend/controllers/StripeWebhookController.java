package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader,
            HttpServletRequest request) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Signature de webhook Stripe invalide: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Erreur lors du traitement du webhook Stripe", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                case "transfer.created":
                    handleTransferCreated(event);
                    break;
                case "transfer.paid":
                    handleTransferPaid(event);
                    break;
                case "transfer.failed":
                    handleTransferFailed(event);
                    break;
                case "payout.paid":
                    handlePayoutPaid(event);
                    break;
                case "payout.failed":
                    handlePayoutFailed(event);
                    break;
                default:
                    log.info("Type d'événement webhook non géré: {}", event.getType());
                    break;
            }
            return ResponseEntity.ok("Webhook traité avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement webhook: {}", event.getType(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur de traitement");
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.info("Payment Intent réussi: {}", paymentIntent.getId());

            try {
                paymentService.confirmPayment(paymentIntent.getId());
                log.info("Paiement confirmé automatiquement via webhook: {}", paymentIntent.getId());
            } catch (Exception e) {
                log.error("Erreur lors de la confirmation automatique du paiement: {}", paymentIntent.getId(), e);
            }
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.warn("Payment Intent échoué: {}", paymentIntent.getId());
        }
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            log.info("Payment Intent annulé: {}", paymentIntent.getId());
        }
    }

    private void handleTransferCreated(Event event) {
        Transfer transfer = (Transfer) event.getDataObjectDeserializer().getObject().orElse(null);
        if (transfer != null) {
            log.info("Transfer créé: {}", transfer.getId());
            Map<String, String> metadata = transfer.getMetadata();
            String retraitId = metadata.get("retrait_id");
            if (retraitId != null) {
                log.info("Transfer créé pour la demande de retrait: {}", retraitId);
            }
        }
    }

    private void handleTransferPaid(Event event) {
        Transfer transfer = (Transfer) event.getDataObjectDeserializer().getObject().orElse(null);
        if (transfer != null) {
            log.info("Transfer payé: {}", transfer.getId());
            Map<String, String> metadata = transfer.getMetadata();
            String retraitId = metadata.get("retrait_id");
            if (retraitId != null) {
                log.info("Transfer confirmé pour la demande de retrait: {}", retraitId);
            }
        }
    }

    private void handleTransferFailed(Event event) {
        Transfer transfer = (Transfer) event.getDataObjectDeserializer().getObject().orElse(null);

        if (transfer != null) {
            log.error("Transfer échoué: {}", transfer.getId());

            Map<String, String> metadata = transfer.getMetadata();
            String retraitId = metadata.get("retrait_id");

            if (retraitId != null) {
                log.error("Transfer échoué pour la demande de retrait: {}", retraitId);
            }
        }
    }

    private void handlePayoutPaid(Event event) {
        log.info("Payout confirmé: événement reçu");
    }

    private void handlePayoutFailed(Event event) {
        log.error("Payout échoué: événement reçu");
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testWebhook() {
        return ResponseEntity.ok(Map.of(
            "status", "Webhook endpoint actif",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
