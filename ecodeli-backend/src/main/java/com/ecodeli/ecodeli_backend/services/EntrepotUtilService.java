package com.ecodeli.ecodeli_backend.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

@Service
public class EntrepotUtilService {

    private static final Logger logger = LoggerFactory.getLogger(EntrepotUtilService.class);

    private static final Map<String, EntrepotInfo> ENTREPOTS = new HashMap<>();

    static {
        ENTREPOTS.put("Paris", new EntrepotInfo(
            48.8566, 2.3522,
            "Entrepôt EcoDeli Paris, 15 Rue de Rivoli, 75001 Paris",
            "Zone Centre", true
        ));
        ENTREPOTS.put("Lyon", new EntrepotInfo(
            45.7640, 4.8357,
            "Entrepôt EcoDeli Lyon, 45 Cours Lafayette, 69003 Lyon",
            "Zone Sud-Est", true
        ));
        ENTREPOTS.put("Marseille", new EntrepotInfo(
            43.2965, 5.3698,
            "Entrepôt EcoDeli Marseille, 25 La Canebière, 13001 Marseille",
            "Zone Sud", true
        ));
        ENTREPOTS.put("Lille", new EntrepotInfo(
            50.6292, 3.0573,
            "Entrepôt EcoDeli Lille, 12 Grand Place, 59000 Lille",
            "Zone Nord", true
        ));
        ENTREPOTS.put("Montpellier", new EntrepotInfo(
            43.6108, 3.8767,
            "Entrepôt EcoDeli Montpellier, 8 Place de la Comédie, 34000 Montpellier",
            "Zone Sud-Ouest", true
        ));
        ENTREPOTS.put("Rennes", new EntrepotInfo(
            48.1173, -1.6778,
            "Entrepôt EcoDeli Rennes, 30 Place des Lices, 35000 Rennes",
            "Zone Ouest", true
        ));
    }

    private final GoogleMapsService googleMapsService;

    public EntrepotUtilService(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    public List<String> getEntrepotsDisponibles() {
        return new ArrayList<>(ENTREPOTS.keySet());
    }

    public List<String> getEntrepotsActifs() {
        return ENTREPOTS.entrySet().stream()
                .filter(entry -> entry.getValue().isActif())
                .map(Map.Entry::getKey)
                .toList();
    }

    public String findOptimalWarehouse(String origin, String destination) {
        logger.info("Recherche de l'entrepôt optimal entre {} et {}", origin, destination);

        String bestWarehouse = "Paris";
        BigDecimal minTotalDistance = BigDecimal.valueOf(Double.MAX_VALUE);

        for (Map.Entry<String, EntrepotInfo> entry : ENTREPOTS.entrySet()) {
            if (!entry.getValue().isActif()) {
                continue;
            }

            String warehouseName = entry.getKey();
            String warehouseAddress = entry.getValue().getAdresse();

            try {
                GoogleMapsService.RouteOptimizationResult result =
                    googleMapsService.calculateOptimalRoute(origin, destination, warehouseAddress);

                if (result.getTotalDistance().compareTo(minTotalDistance) < 0) {
                    minTotalDistance = result.getTotalDistance();
                    bestWarehouse = warehouseName;
                }

            } catch (Exception e) {
                logger.warn("Erreur lors du calcul pour l'entrepôt {}: {}", warehouseName, e.getMessage());
            }
        }

        logger.info("Entrepôt optimal trouvé: {} (distance totale: {} km)", bestWarehouse, minTotalDistance);
        return bestWarehouse;
    }

    public String getEntrepotLePlusProche(Double latitudeExpedi, Double longitudeExpedi) {
        if (latitudeExpedi == null || longitudeExpedi == null) {
            return "Paris";
        }

        String entrepotLePlusProche = "Paris";
        double distanceMin = Double.MAX_VALUE;

        for (Map.Entry<String, EntrepotInfo> entrepot : ENTREPOTS.entrySet()) {
            if (!entrepot.getValue().isActif()) {
                continue;
            }

            double distance = calculerDistance(
                latitudeExpedi, longitudeExpedi,
                entrepot.getValue().getLatitude(), entrepot.getValue().getLongitude()
            );

            if (distance < distanceMin) {
                distanceMin = distance;
                entrepotLePlusProche = entrepot.getKey();
            }
        }

        return entrepotLePlusProche;
    }

    public Double[] getCoordonneesEntrepot(String nomEntrepot) {
        EntrepotInfo info = ENTREPOTS.get(nomEntrepot);
        if (info != null) {
            return new Double[]{info.getLatitude(), info.getLongitude()};
        }
        return ENTREPOTS.get("Paris") != null ?
            new Double[]{ENTREPOTS.get("Paris").getLatitude(), ENTREPOTS.get("Paris").getLongitude()} :
            new Double[]{48.8566, 2.3522};
    }

    public String getAdresseEntrepot(String nomEntrepot) {
        EntrepotInfo info = ENTREPOTS.get(nomEntrepot);
        return info != null ? info.getAdresse() : "Entrepôt EcoDeli Paris, 15 Rue de Rivoli, 75001 Paris";
    }

    public EntrepotInfo getInfoEntrepot(String nomEntrepot) {
        return ENTREPOTS.get(nomEntrepot);
    }

    public DeliveryComparisonResult compareDeliveryOptions(String origin, String destination) {
        GoogleMapsService.DistanceCalculationResult directResult =
            googleMapsService.calculateDistance(origin, destination);

        String optimalWarehouse = findOptimalWarehouse(origin, destination);

        GoogleMapsService.RouteOptimizationResult partialResult =
            googleMapsService.calculateOptimalRoute(origin, destination, getAdresseEntrepot(optimalWarehouse));

        return new DeliveryComparisonResult(
            directResult.getDistance(),
            directResult.getDuration(),
            partialResult.getTotalDistance(),
            partialResult.getTotalDuration(),
            partialResult.getSegment1Distance(),
            partialResult.getSegment1Duration(),
            partialResult.getSegment2Distance(),
            partialResult.getSegment2Duration(),
            optimalWarehouse
        );
    }

    public List<Map<String, Object>> findTop3OptimalWarehouses(String origin, String destination) {
        logger.info("Recherche des 3 meilleurs entrepôts entre {} et {}", origin, destination);

        List<Map<String, Object>> warehouseScores = new ArrayList<>();

        for (Map.Entry<String, EntrepotInfo> entry : ENTREPOTS.entrySet()) {
            if (!entry.getValue().isActif()) {
                continue;
            }
            String warehouseName = entry.getKey();
            String warehouseAddress = entry.getValue().getAdresse();

            try {
                GoogleMapsService.RouteOptimizationResult result =
                    googleMapsService.calculateOptimalRoute(origin, destination, warehouseAddress);

                double score = result.getTotalDistance().doubleValue();

                Map<String, Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put("name", warehouseName);
                warehouseInfo.put("address", warehouseAddress);
                warehouseInfo.put("latitude", entry.getValue().getLatitude());
                warehouseInfo.put("longitude", entry.getValue().getLongitude());
                warehouseInfo.put("zone", entry.getValue().getZone());
                warehouseInfo.put("totalDistance", result.getTotalDistance());
                warehouseInfo.put("totalDuration", result.getTotalDuration());
                warehouseInfo.put("segment1Distance", result.getSegment1Distance());
                warehouseInfo.put("segment1Duration", result.getSegment1Duration());
                warehouseInfo.put("segment2Distance", result.getSegment2Distance());
                warehouseInfo.put("segment2Duration", result.getSegment2Duration());
                warehouseInfo.put("score", score);
                warehouseInfo.put("recommended", false);

                warehouseScores.add(warehouseInfo);

            } catch (Exception e) {
                logger.warn("Erreur lors du calcul pour l'entrepôt {}: {}", warehouseName, e.getMessage());
            }
        }

        warehouseScores.sort((a, b) ->
            Double.compare((Double) a.get("score"), (Double) b.get("score"))
        );

        List<Map<String, Object>> top3 = warehouseScores.stream()
            .limit(3)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!top3.isEmpty()) {
            top3.get(0).put("recommended", true);
        }

        logger.info("Top 3 entrepôts trouvés: {}",
            top3.stream().map(w -> w.get("name")).collect(java.util.stream.Collectors.toList()));

        return top3;
    }

    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static class EntrepotInfo {
        private final double latitude;
        private final double longitude;
        private final String adresse;
        private final String zone;
        private final boolean actif;

        public EntrepotInfo(double latitude, double longitude, String adresse, String zone, boolean actif) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.adresse = adresse;
            this.zone = zone;
            this.actif = actif;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getAdresse() { return adresse; }
        public String getZone() { return zone; }
        public boolean isActif() { return actif; }
    }

    public static class DeliveryComparisonResult {
        private final BigDecimal directDistance;
        private final int directDuration;
        private final BigDecimal partialTotalDistance;
        private final int partialTotalDuration;
        private final BigDecimal segment1Distance;
        private final int segment1Duration;
        private final BigDecimal segment2Distance;
        private final int segment2Duration;
        private final String optimalWarehouse;

        public DeliveryComparisonResult(BigDecimal directDistance, int directDuration,
                                      BigDecimal partialTotalDistance, int partialTotalDuration,
                                      BigDecimal segment1Distance, int segment1Duration,
                                      BigDecimal segment2Distance, int segment2Duration,
                                      String optimalWarehouse) {
            this.directDistance = directDistance;
            this.directDuration = directDuration;
            this.partialTotalDistance = partialTotalDistance;
            this.partialTotalDuration = partialTotalDuration;
            this.segment1Distance = segment1Distance;
            this.segment1Duration = segment1Duration;
            this.segment2Distance = segment2Distance;
            this.segment2Duration = segment2Duration;
            this.optimalWarehouse = optimalWarehouse;
        }

        public BigDecimal getDirectDistance() { return directDistance; }
        public int getDirectDuration() { return directDuration; }
        public BigDecimal getPartialTotalDistance() { return partialTotalDistance; }
        public int getPartialTotalDuration() { return partialTotalDuration; }
        public BigDecimal getSegment1Distance() { return segment1Distance; }
        public int getSegment1Duration() { return segment1Duration; }
        public BigDecimal getSegment2Distance() { return segment2Distance; }
        public int getSegment2Duration() { return segment2Duration; }
        public String getOptimalWarehouse() { return optimalWarehouse; }

        public boolean isPartialBetter() {
            return partialTotalDistance.compareTo(directDistance.multiply(BigDecimal.valueOf(1.1))) <= 0;
        }

        public BigDecimal getDistanceSavings() {
            return directDistance.subtract(partialTotalDistance);
        }

        public int getTimeDifference() {
            return partialTotalDuration - directDuration;
        }
    }
}
