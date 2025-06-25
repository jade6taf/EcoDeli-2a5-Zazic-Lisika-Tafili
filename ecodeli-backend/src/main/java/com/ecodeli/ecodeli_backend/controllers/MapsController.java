package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Coordinates;
import com.ecodeli.ecodeli_backend.services.GoogleMapsService;
import com.ecodeli.ecodeli_backend.services.TarificationService;
import com.ecodeli.ecodeli_backend.services.EntrepotUtilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/maps")
@CrossOrigin(origins = "http://localhost:5173")
public class MapsController {

    private final GoogleMapsService googleMapsService;
    private final TarificationService tarificationService;
    private final EntrepotUtilService entrepotUtilService;

    public MapsController(GoogleMapsService googleMapsService, 
                         TarificationService tarificationService,
                         EntrepotUtilService entrepotUtilService) {
        this.googleMapsService = googleMapsService;
        this.tarificationService = tarificationService;
        this.entrepotUtilService = entrepotUtilService;
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody PriceCalculationRequest request) {
        try {
            if (request.isPartialDelivery()) {
                TarificationService.PriceCalculationResult directResult =
                    tarificationService.calculateDirectDeliveryPrice(
                        request.getOrigin(),
                        request.getDestination(),
                        request.getColis(),
                        request.isUrgent()
                    );

                String warehouse = request.getWarehouse();
                if (warehouse == null || warehouse.isEmpty()) {
                    warehouse = entrepotUtilService.findOptimalWarehouse(request.getOrigin(), request.getDestination());
                }

                BigDecimal totalPrice = directResult.getTotalPrice();
                BigDecimal segment1Price = totalPrice.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal segment2Price = totalPrice.subtract(segment1Price);
                BigDecimal segment1Distance = directResult.getDistance().divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal segment2Distance = directResult.getDistance().subtract(segment1Distance);

                Map<String, Object> response = new HashMap<>();
                response.put("type", "PARTIELLE");
                response.put("totalPrice", totalPrice);
                response.put("segment1Price", segment1Price);
                response.put("segment2Price", segment2Price);
                response.put("totalDistance", directResult.getDistance());
                response.put("segment1Distance", segment1Distance);
                response.put("segment2Distance", segment2Distance);
                response.put("totalDuration", directResult.getDuration());
                response.put("segment1Duration", directResult.getDuration() / 2);
                response.put("segment2Duration", directResult.getDuration() / 2);
                response.put("warehouse", warehouse);
                response.put("directPrice", directResult.getTotalPrice());
                response.put("directDistance", directResult.getDistance());
                response.put("directDuration", directResult.getDuration());
                response.put("savings", BigDecimal.ZERO);
                response.put("isMoreExpensive", false);

                return ResponseEntity.ok(response);
            } else {
                TarificationService.PriceCalculationResult result =
                    tarificationService.calculateDirectDeliveryPrice(
                        request.getOrigin(),
                        request.getDestination(),
                        request.getColis(),
                        request.isUrgent()
                    );

                return ResponseEntity.ok(Map.of(
                    "type", "DIRECTE",
                    "totalPrice", result.getTotalPrice(),
                    "distance", result.getDistance(),
                    "duration", result.getDuration(),
                    "accurate", result.isAccurate()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du calcul du prix: " + e.getMessage());
        }
    }

    @GetMapping("/optimal-warehouse")
    public ResponseEntity<?> getOptimalWarehouse(@RequestParam String origin, @RequestParam String destination) {
        try {
            String optimalWarehouse = entrepotUtilService.findOptimalWarehouse(origin, destination);
            EntrepotUtilService.EntrepotInfo info = entrepotUtilService.getInfoEntrepot(optimalWarehouse);

            return ResponseEntity.ok(Map.of(
                "warehouse", optimalWarehouse,
                "address", info.getAdresse(),
                "coordinates", new double[]{info.getLatitude(), info.getLongitude()},
                "zone", info.getZone()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la recherche d'entrepôt: " + e.getMessage());
        }
    }

    @PostMapping("/compare-delivery-options")
    public ResponseEntity<?> compareDeliveryOptions(@RequestBody DeliveryComparisonRequest request) {
        try {
            TarificationService.PriceCalculationResult directResult =
                tarificationService.calculateDirectDeliveryPrice(
                    request.getOrigin(), request.getDestination(), request.getColis(), request.isUrgent()
                );

            String optimalWarehouse = entrepotUtilService.findOptimalWarehouse(request.getOrigin(), request.getDestination());

            BigDecimal directPrice = directResult.getTotalPrice();
            BigDecimal segment1Price = directPrice.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal segment2Price = directPrice.subtract(segment1Price);
            BigDecimal segment1Distance = directResult.getDistance().divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal segment2Distance = directResult.getDistance().subtract(segment1Distance);

            Map<String, Object> response = new HashMap<>();

            Map<String, Object> direct = new HashMap<>();
            direct.put("price", directPrice);
            direct.put("distance", directResult.getDistance());
            direct.put("duration", directResult.getDuration());
            direct.put("type", "DIRECTE");
            response.put("direct", direct);

            Map<String, Object> partial = new HashMap<>();
            partial.put("totalPrice", directPrice);
            partial.put("segment1Price", segment1Price);
            partial.put("segment2Price", segment2Price);
            partial.put("distance", directResult.getDistance());
            partial.put("duration", directResult.getDuration());
            partial.put("warehouse", optimalWarehouse);
            partial.put("segment1Distance", segment1Distance);
            partial.put("segment2Distance", segment2Distance);
            partial.put("segment1Duration", directResult.getDuration() / 2);
            partial.put("segment2Duration", directResult.getDuration() / 2);
            partial.put("type", "PARTIELLE");
            response.put("partial", partial);

            response.put("recommendation", "DIRECTE");
            response.put("savings", BigDecimal.ZERO);
            response.put("timeDifference", 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la comparaison: " + e.getMessage());
        }
    }

    @PostMapping("/geocode")
    public ResponseEntity<?> geocodeAddress(@RequestBody AddressRequest request) {
        try {
            Optional<Coordinates> coords = googleMapsService.geocodeAddress(request.getAddress());
            if (coords.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "latitude", coords.get().getLatitude(),
                    "longitude", coords.get().getLongitude(),
                    "address", request.getAddress()
                ));
            } else {
                return ResponseEntity.badRequest().body("Adresse non trouvée");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du géocodage: " + e.getMessage());
        }
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<?> getAddressSuggestions(@RequestParam String input) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Utilisez l'autocomplétion frontend avec Google Maps API",
                "input", input
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'autocomplétion: " + e.getMessage());
        }
    }

    @PostMapping("/distance")
    public ResponseEntity<?> calculateDistance(@RequestBody DistanceRequest request) {
        try {
            GoogleMapsService.DistanceCalculationResult result =
                googleMapsService.calculateDistance(request.getOrigin(), request.getDestination());

            return ResponseEntity.ok(Map.of(
                "distance", result.getDistance(),
                "duration", result.getDuration(),
                "accurate", result.isAccurate()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du calcul de distance: " + e.getMessage());
        }
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<Map<String, Object>>> getAvailableWarehouses() {
        List<String> warehouses = entrepotUtilService.getEntrepotsActifs();

        List<Map<String, Object>> warehouseInfo = warehouses.stream()
            .map(name -> {
                EntrepotUtilService.EntrepotInfo info = entrepotUtilService.getInfoEntrepot(name);
                Map<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("address", info.getAdresse());
                map.put("latitude", info.getLatitude());
                map.put("longitude", info.getLongitude());
                map.put("zone", info.getZone());
                map.put("active", info.isActif());
                return map;
            })
            .toList();

        return ResponseEntity.ok(warehouseInfo);
    }

    @GetMapping("/optimal-warehouses")
    public ResponseEntity<?> getOptimalWarehouses(
            @RequestParam String origin,
            @RequestParam String destination) {
        try {
            List<Map<String, Object>> optimalWarehouses =
                entrepotUtilService.findTop3OptimalWarehouses(origin, destination);

            return ResponseEntity.ok(optimalWarehouses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erreur lors de la recherche d'entrepôts optimaux: " + e.getMessage());
        }
    }

    public static class PriceCalculationRequest {
        private String origin;
        private String destination;
        private String warehouse;
        private boolean partialDelivery;
        private boolean urgent;
        private Colis colis;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public String getWarehouse() { return warehouse; }
        public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
        public boolean isPartialDelivery() { return partialDelivery; }
        public void setPartialDelivery(boolean partialDelivery) { this.partialDelivery = partialDelivery; }
        public boolean isUrgent() { return urgent; }
        public void setUrgent(boolean urgent) { this.urgent = urgent; }
        public Colis getColis() { return colis; }
        public void setColis(Colis colis) { this.colis = colis; }
    }

    public static class DeliveryComparisonRequest {
        private String origin;
        private String destination;
        private boolean urgent;
        private Colis colis;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public boolean isUrgent() { return urgent; }
        public void setUrgent(boolean urgent) { this.urgent = urgent; }
        public Colis getColis() { return colis; }
        public void setColis(Colis colis) { this.colis = colis; }
    }

    public static class AddressRequest {
        private String address;

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class DistanceRequest {
        private String origin;
        private String destination;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
    }
}