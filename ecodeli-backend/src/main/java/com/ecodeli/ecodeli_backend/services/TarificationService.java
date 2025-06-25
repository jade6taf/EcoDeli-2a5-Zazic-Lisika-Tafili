package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Colis;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TarificationService {

    // Tarif fixe 80 centimes par kilomètre
    private static final BigDecimal PRIX_PAR_KM = new BigDecimal("0.80");

    private final GoogleMapsService googleMapsService;

    public TarificationService(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public PriceCalculationResult calculateDirectDeliveryPrice(String origin, String destination, Colis colis, boolean urgent) {
        GoogleMapsService.DistanceCalculationResult distanceResult =
            googleMapsService.calculateDistance(origin, destination);

        BigDecimal simplePrice = distanceResult.getDistance().multiply(PRIX_PAR_KM);

        return new PriceCalculationResult(
            simplePrice,
            distanceResult.getDistance(),
            distanceResult.getDuration(),
            distanceResult.isAccurate(),
            "DIRECTE"
        );
    }

    public static class PriceCalculationResult {
        private final BigDecimal totalPrice;
        private final BigDecimal distance;
        private final int duration;
        private final boolean accurate;
        private final String type;

        public PriceCalculationResult(BigDecimal totalPrice, BigDecimal distance, int duration, boolean accurate, String type) {
            this.totalPrice = totalPrice;
            this.distance = distance;
            this.duration = duration;
            this.accurate = accurate;
            this.type = type;
        }

        public BigDecimal getTotalPrice() { return totalPrice; }
        public BigDecimal getDistance() { return distance; }
        public int getDuration() { return duration; }
        public boolean isAccurate() { return accurate; }
        public String getType() { return type; }
    }

    public static class PartialDeliveryPriceResult {
        private final BigDecimal totalPrice;
        private final BigDecimal segment1Price;
        private final BigDecimal segment2Price;
        private final BigDecimal totalDistance;
        private final BigDecimal segment1Distance;
        private final BigDecimal segment2Distance;
        private final int totalDuration;
        private final int segment1Duration;
        private final int segment2Duration;
        private final BigDecimal directPrice;
        private final BigDecimal directDistance;
        private final int directDuration;
        private final String warehouse;

        public PartialDeliveryPriceResult(BigDecimal totalPrice, BigDecimal segment1Price, BigDecimal segment2Price,
                                        BigDecimal totalDistance, BigDecimal segment1Distance, BigDecimal segment2Distance,
                                        int totalDuration, int segment1Duration, int segment2Duration,
                                        BigDecimal directPrice, BigDecimal directDistance, int directDuration,
                                        String warehouse) {
            this.totalPrice = totalPrice;
            this.segment1Price = segment1Price;
            this.segment2Price = segment2Price;
            this.totalDistance = totalDistance;
            this.segment1Distance = segment1Distance;
            this.segment2Distance = segment2Distance;
            this.totalDuration = totalDuration;
            this.segment1Duration = segment1Duration;
            this.segment2Duration = segment2Duration;
            this.directPrice = directPrice;
            this.directDistance = directDistance;
            this.directDuration = directDuration;
            this.warehouse = warehouse;
        }

        public BigDecimal getTotalPrice() { return totalPrice; }
        public BigDecimal getSegment1Price() { return segment1Price; }
        public BigDecimal getSegment2Price() { return segment2Price; }
        public BigDecimal getTotalDistance() { return totalDistance; }
        public BigDecimal getSegment1Distance() { return segment1Distance; }
        public BigDecimal getSegment2Distance() { return segment2Distance; }
        public int getTotalDuration() { return totalDuration; }
        public int getSegment1Duration() { return segment1Duration; }
        public int getSegment2Duration() { return segment2Duration; }
        public BigDecimal getDirectPrice() { return directPrice; }
        public BigDecimal getDirectDistance() { return directDistance; }
        public int getDirectDuration() { return directDuration; }
        public String getWarehouse() { return warehouse; }

        public boolean isPartialMoreExpensive() {
            return totalPrice.compareTo(directPrice) > 0;
        }

        public BigDecimal getSavings() {
            return directPrice.subtract(totalPrice);
        }
    }
}