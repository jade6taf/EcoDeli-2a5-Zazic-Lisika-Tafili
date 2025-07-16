package com.ecodeli.analytics.services;

import com.github.javafaker.Faker;
import com.ecodeli.analytics.dto.ClientAnalyticsDto;
import com.ecodeli.analytics.dto.PrestationAnalyticsDto;
import com.ecodeli.analytics.dto.UserDto;
import com.ecodeli.analytics.dto.LivraisonDto;
import com.ecodeli.analytics.dto.ServiceDto;
import com.ecodeli.analytics.enums.ServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataGeneratorService {

    private final Faker faker = new Faker(new Locale("fr"));

    public List<Map<String, Object>> generateUsers(int count, String type) {
        List<Map<String, Object>> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", i + 1);
            user.put("nom", faker.name().lastName());
            user.put("prenom", faker.name().firstName());
            user.put("email", faker.internet().emailAddress());
            user.put("type", type);
            user.put("dateInscription", generateRandomDate());
            user.put("statut", faker.options().option("ACTIF", "INACTIF", "SUSPENDU"));

            switch (type.toLowerCase()) {
                case "client":
                    user.put("nbCommandes", faker.number().numberBetween(1, 50));
                    user.put("totalDepense", faker.number().randomDouble(2, 50, 2000));
                    break;
                case "commercant":
                    user.put("nomEntreprise", faker.company().name());
                    user.put("secteur", faker.options().option("Alimentation", "Mode", "Électronique", "Livres", "Beauté"));
                    user.put("chiffreAffaires", faker.number().randomDouble(2, 5000, 100000));
                    break;
                case "livreur":
                    user.put("nbLivraisons", faker.number().numberBetween(5, 200));
                    user.put("note", faker.number().randomDouble(1, 30, 50) / 10.0); // Note sur 5
                    user.put("vehicule", faker.options().option("Vélo", "Scooter", "Voiture", "À pied"));
                    break;
            }

            users.add(user);
        }
        return users;
    }

    public List<Map<String, Object>> generateLivraisons(int count) {
        List<Map<String, Object>> livraisons = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map<String, Object> livraison = new HashMap<>();
            livraison.put("id", i + 1);
            livraison.put("expediteur", faker.name().fullName());
            livraison.put("destinataire", faker.name().fullName());
            livraison.put("adresseDepart", faker.address().fullAddress());
            livraison.put("adresseArrivee", faker.address().fullAddress());
            livraison.put("type", faker.options().option("DIRECTE", "PARTIELLE"));
            livraison.put("statut", faker.options().option("EN_COURS", "TERMINEE", "ANNULEE", "ATTENTE_SEGMENT_2"));
            livraison.put("prix", faker.number().randomDouble(2, 5, 100));
            livraison.put("poids", faker.number().randomDouble(1, 1, 50));
            livraison.put("dateCreation", generateRandomDate());
            livraison.put("dateLivraison", generateRandomDate());
            livraison.put("distanceKm", faker.number().numberBetween(1, 500));
            livraison.put("evaluationClient", faker.number().numberBetween(1, 5));
            livraisons.add(livraison);
        }

        return livraisons;
    }

    public List<Map<String, Object>> generateServices(int count) {
        List<Map<String, Object>> services = new ArrayList<>();

        String[] typesServices = {
            "Transport de personne", "Courses à domicile", "Garde d'animaux",
            "Petits travaux", "Transfert aéroport", "Achat à l'étranger"
        };

        for (int i = 0; i < count; i++) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", i + 1);
            service.put("nom", faker.options().option(typesServices));
            service.put("description", faker.lorem().sentence(10));
            service.put("prix", faker.number().randomDouble(2, 10, 200));
            service.put("duree", faker.number().numberBetween(30, 480)); // en minutes
            service.put("prestataire", faker.name().fullName());
            service.put("client", faker.name().fullName());
            service.put("dateService", generateRandomDate());
            service.put("statut", faker.options().option("PLANIFIE", "EN_COURS", "TERMINE", "ANNULE"));
            service.put("evaluation", faker.number().numberBetween(1, 5));

            services.add(service);
        }
        return services;
    }


    public Map<String, Object> generateAggregatedStats(
            List<Map<String, Object>> users,
            List<Map<String, Object>> livraisons,
            List<Map<String, Object>> services) {

        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> usersByType = new HashMap<>();
        usersByType.put("clients", users.stream().filter(u -> "CLIENT".equals(u.get("type"))).count());
        usersByType.put("commercants", users.stream().filter(u -> "COMMERCANT".equals(u.get("type"))).count());
        usersByType.put("livreurs", users.stream().filter(u -> "LIVREUR".equals(u.get("type"))).count());
        usersByType.put("prestataires", users.stream().filter(u -> "PRESTATAIRE".equals(u.get("type"))).count());
        stats.put("usersByType", usersByType);
        stats.put("totalUsers", users.size());

        Map<String, Long> livraisonsByStatut = new HashMap<>();
        livraisonsByStatut.put("EN_COURS", livraisons.stream().filter(l -> "EN_COURS".equals(l.get("statut"))).count());
        livraisonsByStatut.put("TERMINEE", livraisons.stream().filter(l -> "TERMINEE".equals(l.get("statut"))).count());
        livraisonsByStatut.put("ANNULEE", livraisons.stream().filter(l -> "ANNULEE".equals(l.get("statut"))).count());

        double revenusTotal = livraisons.stream()
            .mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0))
            .sum();
        stats.put("livraisonsByStatut", livraisonsByStatut);
        stats.put("totalLivraisons", livraisons.size());
        stats.put("revenusTotal", revenusTotal);

        Map<String, Long> servicesByType = new HashMap<>();
        for (Map<String, Object> service : services) {
            String type = (String) service.get("nom");
            servicesByType.merge(type, 1L, Long::sum);
        }
        double revenusServices = services.stream()
            .mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0))
            .sum();
        stats.put("servicesByType", servicesByType);
        stats.put("totalServices", services.size());
        stats.put("revenusServices", revenusServices);

        return stats;
    }

    private String generateRandomDate() {
        LocalDateTime date = LocalDateTime.now()
            .minusDays(faker.number().numberBetween(1, 365));
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Map<String, Object> generateCompleteDataset() {

        List<Map<String, Object>> clients = generateUsers(30, "CLIENT");
        List<Map<String, Object>> commercants = generateUsers(30, "COMMERCANT");
        List<Map<String, Object>> livreurs = generateUsers(30, "LIVREUR");
        List<Map<String, Object>> livraisons = generateLivraisons(50);
        List<Map<String, Object>> services = generateServices(40);

        List<Map<String, Object>> allUsers = new ArrayList<>();
        allUsers.addAll(clients);
        allUsers.addAll(commercants);
        allUsers.addAll(livreurs);

        Map<String, Object> stats = generateAggregatedStats(allUsers, livraisons, services);

        Map<String, Object> dataset = new HashMap<>();
        dataset.put("users", allUsers);
        dataset.put("clients", clients);
        dataset.put("commercants", commercants);
        dataset.put("livreurs", livreurs);
        dataset.put("livraisons", livraisons);
        dataset.put("services", services);
        dataset.put("stats", stats);
        dataset.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return dataset;
    }
}
