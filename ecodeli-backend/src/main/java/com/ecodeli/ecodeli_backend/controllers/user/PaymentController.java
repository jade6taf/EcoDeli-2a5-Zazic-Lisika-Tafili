package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Client;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.services.StripePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final StripePaymentService stripePaymentService;
    private final AnnonceRepository annonceRepository;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @PostMapping("/create-intent/{annonceId}")
    public ResponseEntity<?> createPaymentIntent(@PathVariable Integer annonceId) {
        try {
            Map<String, Object> paymentData = stripePaymentService.createPaymentIntent(annonceId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clientSecret", paymentData.get("clientSecret"));
            response.put("paymentIntentId", paymentData.get("paymentIntentId"));
            response.put("amount", paymentData.get("amount"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création du PaymentIntent: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/confirm/{annonceId}")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Integer annonceId,
            @RequestBody Map<String, String> payload) {

        try {
            String paymentIntentId = payload.get("paymentIntentId");
            String ibanClient = payload.get("ibanClient");

            if (paymentIntentId == null || ibanClient == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "PaymentIntentId et IBAN client requis");
                return ResponseEntity.badRequest().body(error);
            }

            boolean paymentSuccess = stripePaymentService.confirmPayment(paymentIntentId, annonceId);

            if (paymentSuccess) {
                Annonce annonce = annonceRepository.findById(annonceId)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

                if (annonce.getExpediteur() instanceof Client) {
                    Client client = (Client) annonce.getExpediteur();
                    // Voi si on ajoute un champ IBAN au modèle Client si nécessaire
                }

                annonceRepository.save(annonce);

                log.info("Paiement confirmé pour l'annonce {}", annonceId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Paiement confirmé avec succès");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Le paiement n'a pas pu être confirmé");
                return ResponseEntity.badRequest().body(error);
            }

        } catch (Exception e) {
            log.error("Erreur lors de la confirmation du paiement: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getStripeConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("publishableKey", stripePublishableKey);
        return ResponseEntity.ok(config);
    }
}
