package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Coordinates;
import com.google.maps.*;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class GoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

    @Value("${google.maps.api.key}")
    private String apiKey;

    private GeoApiContext context;

    public GoogleMapsService(@Value("${google.maps.api.key}") String apiKey) {
        this.apiKey = apiKey;
        initializeContext();
    }

    private void initializeContext() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
        }
    }

    public DistanceCalculationResult calculateDistance(String origin, String destination) {
        if (context == null) {
            logger.warn("Google Maps API non configurée, utilisation de valeurs par défaut");
            return getDefaultCalculation();
        }

        try {
            DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(context)
                    .origins(origin)
                    .destinations(destination)
                    .units(Unit.METRIC)
                    .mode(TravelMode.DRIVING);

            DistanceMatrix result = request.await();

            if (result.rows.length > 0 && result.rows[0].elements.length > 0) {
                DistanceMatrixElement element = result.rows[0].elements[0];

                if (element.status == DistanceMatrixElementStatus.OK) {
                    long distanceMeters = element.distance.inMeters;
                    long durationSeconds = element.duration.inSeconds;

                    double distanceKm = distanceMeters / 1000.0;
                    int durationMinutes = (int) (durationSeconds / 60);

                    return new DistanceCalculationResult(
                        BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP),
                        durationMinutes,
                        true
                    );
                }
            }

            logger.warn("Impossible de calculer la distance entre {} et {}", origin, destination);
            return getDefaultCalculation();

        } catch (Exception e) {
            logger.error("Erreur lors du calcul de distance: {}", e.getMessage());
            return getDefaultCalculation();
        }
    }

    public Optional<Coordinates> geocodeAddress(String address) {
        if (context == null) {
            logger.warn("Google Maps API non configurée pour le géocodage");
            return Optional.empty();
        }

        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if (results.length > 0) {
                LatLng location = results[0].geometry.location;
                return Optional.of(new Coordinates(location.lat, location.lng));
            }

        } catch (Exception e) {
            logger.error("Erreur lors du géocodage de l'adresse {}: {}", address, e.getMessage());
        }

        return Optional.empty();
    }

    public RouteOptimizationResult calculateOptimalRoute(String origin, String destination, String waypoint) {
        if (context == null) {
            return getDefaultRouteOptimization(origin, destination, waypoint);
        }

        try {
            DistanceCalculationResult directRoute = calculateDistance(origin, destination);

            DistanceCalculationResult segment1 = calculateDistance(origin, waypoint);
            DistanceCalculationResult segment2 = calculateDistance(waypoint, destination);

            BigDecimal totalDistanceWithWaypoint = segment1.getDistance().add(segment2.getDistance());
            int totalDurationWithWaypoint = segment1.getDuration() + segment2.getDuration();

            return new RouteOptimizationResult(
                directRoute.getDistance(),
                directRoute.getDuration(),
                segment1.getDistance(),
                segment1.getDuration(),
                segment2.getDistance(),
                segment2.getDuration(),
                totalDistanceWithWaypoint,
                totalDurationWithWaypoint
            );

        } catch (Exception e) {
            logger.error("Erreur lors du calcul d'itinéraire optimal: {}", e.getMessage());
            return getDefaultRouteOptimization(origin, destination, waypoint);
        }
    }

    private DistanceCalculationResult getDefaultCalculation() {
        return new DistanceCalculationResult(
            BigDecimal.valueOf(50.0),
            60,
            false
        );
    }

    private RouteOptimizationResult getDefaultRouteOptimization(String origin, String destination, String waypoint) {
        return new RouteOptimizationResult(
            BigDecimal.valueOf(50.0), 60,  // Route directe
            BigDecimal.valueOf(25.0), 30,  // Segment 1
            BigDecimal.valueOf(25.0), 30,  // Segment 2
            BigDecimal.valueOf(50.0), 60   // Total avec waypoint
        );
    }

    public static class DistanceCalculationResult {
        private final BigDecimal distance;
        private final int duration;
        private final boolean accurate;

        public DistanceCalculationResult(BigDecimal distance, int duration, boolean accurate) {
            this.distance = distance;
            this.duration = duration;
            this.accurate = accurate;
        }

        public BigDecimal getDistance() { return distance; }
        public int getDuration() { return duration; }
        public boolean isAccurate() { return accurate; }
    }

    public static class RouteOptimizationResult {
        private final BigDecimal directDistance;
        private final int directDuration;
        private final BigDecimal segment1Distance;
        private final int segment1Duration;
        private final BigDecimal segment2Distance;
        private final int segment2Duration;
        private final BigDecimal totalDistance;
        private final int totalDuration;

        public RouteOptimizationResult(BigDecimal directDistance, int directDuration,
                                     BigDecimal segment1Distance, int segment1Duration,
                                     BigDecimal segment2Distance, int segment2Duration,
                                     BigDecimal totalDistance, int totalDuration) {
            this.directDistance = directDistance;
            this.directDuration = directDuration;
            this.segment1Distance = segment1Distance;
            this.segment1Duration = segment1Duration;
            this.segment2Distance = segment2Distance;
            this.segment2Duration = segment2Duration;
            this.totalDistance = totalDistance;
            this.totalDuration = totalDuration;
        }

        public BigDecimal getDirectDistance() { return directDistance; }
        public int getDirectDuration() { return directDuration; }
        public BigDecimal getSegment1Distance() { return segment1Distance; }
        public int getSegment1Duration() { return segment1Duration; }
        public BigDecimal getSegment2Distance() { return segment2Distance; }
        public int getSegment2Duration() { return segment2Duration; }
        public BigDecimal getTotalDistance() { return totalDistance; }
        public int getTotalDuration() { return totalDuration; }
    }
}