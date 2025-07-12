package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final AnnonceRepository annonceRepository;

    public Map<String, Object> createPaymentIntent(Integer annonceId) {
        try {
            Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

            BigDecimal prixEuros = annonce.getPrixUnitaire();
            long montantCentimes = prixEuros.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .putMetadata("annonceId", annonceId.toString())
                .putMetadata("clientId", annonce.getExpediteur().getIdUtilisateur().toString())
                .setDescription("Paiement pour livraison: " + annonce.getTitre())
                .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("PaymentIntent créé: {} pour annonce: {}", paymentIntent.getId(), annonceId);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", montantCentimes);
            return response;

        } catch (StripeException e) {
            log.error("Erreur lors de la création du PaymentIntent: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création du paiement: " + e.getMessage());
        }
    }

    public boolean confirmPayment(String paymentIntentId, Integer annonceId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("Paiement confirmé: {} pour annonce: {}", paymentIntentId, annonceId);
                return true;
            }

            return false;
        } catch (StripeException e) {
            log.error("Erreur lors de la confirmation du paiement: {}", e.getMessage());
            return false;
        }
    }
}
