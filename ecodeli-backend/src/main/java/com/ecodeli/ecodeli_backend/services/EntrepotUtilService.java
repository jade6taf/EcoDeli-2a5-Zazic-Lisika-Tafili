package com.ecodeli.ecodeli_backend.services;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EntrepotUtilService {

    // Liste des entrepôts disponibles avec leurs coordonnées approximatives
    private static final Map<String, Double[]> ENTREPOTS = new HashMap<>();

    static {
        ENTREPOTS.put("Paris", new Double[]{48.8566, 2.3522});
        ENTREPOTS.put("Lyon", new Double[]{45.7640, 4.8357});
        ENTREPOTS.put("Marseille", new Double[]{43.2965, 5.3698});
        ENTREPOTS.put("Lille", new Double[]{50.6292, 3.0573});
        ENTREPOTS.put("Montpellier", new Double[]{43.6108, 3.8767});
        ENTREPOTS.put("Rennes", new Double[]{48.1173, -1.6778});
    }

    public List<String> getEntrepotsDisponibles() {
        return Arrays.asList("Paris", "Lyon", "Marseille", "Lille", "Montpellier", "Rennes");
    }

    public String getEntrepotLePlusProche(Double latitudeExpedi, Double longitudeExpedi) {
        if (latitudeExpedi == null || longitudeExpedi == null) {
            return "Paris";
        }

        String entrepotLePlusProche = "Paris";
        double distanceMin = Double.MAX_VALUE;

        for (Map.Entry<String, Double[]> entrepot : ENTREPOTS.entrySet()) {
            double distance = calculerDistance(
                latitudeExpedi, longitudeExpedi,
                entrepot.getValue()[0], entrepot.getValue()[1]
            );

            if (distance < distanceMin) {
                distanceMin = distance;
                entrepotLePlusProche = entrepot.getKey();
            }
        }

        return entrepotLePlusProche;
    }

    public Double[] getCoordonneesEntrepot(String nomEntrepot) {
        return ENTREPOTS.get(nomEntrepot);
    }

    // Calcul de distance simple (formule de Haversine)
    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
