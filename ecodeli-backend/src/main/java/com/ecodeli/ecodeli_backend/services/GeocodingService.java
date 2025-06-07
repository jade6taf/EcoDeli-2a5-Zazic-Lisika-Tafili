package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class GeocodingService {

    private WebClient webClient;

    @Value("${nominatim.api.url:https://nominatim.openstreetmap.org}")
    private String nominatimApiUrl;

    @Value("${application.name:EcodeliBackend}")
    private String applicationName;

    private final WebClient.Builder webClientBuilder;

    public GeocodingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @jakarta.annotation.PostConstruct
    private void init() {
        this.webClient = this.webClientBuilder
            .baseUrl(this.nominatimApiUrl)
            .defaultHeader("User-Agent", this.applicationName + " Geocoding Service")
            .build();
    }

    public Mono<List<Map<String, Object>>> searchAddresses(String query) {
        if (query == null || query.trim().length() < 3) {
            return Mono.just(new ArrayList<>());
        }

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query.trim())
                        .queryParam("format", "jsonv2")
                        .queryParam("addressdetails", 1)
                        .queryParam("limit", 5)
                        .queryParam("countrycodes", "fr")
                        .queryParam("dedupe", 1)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(responseArray -> {
                    List<Map<String, Object>> suggestions = new ArrayList<>();
                    if (responseArray.isArray()) {
                        for (JsonNode result : responseArray) {
                            Map<String, Object> suggestion = new HashMap<>();
                            suggestion.put("display_name", result.path("display_name").asText());
                            suggestion.put("lat", result.path("lat").asDouble());
                            suggestion.put("lon", result.path("lon").asDouble());

                            JsonNode address = result.path("address");
                            if (!address.isMissingNode()) {
                                Map<String, String> addressComponents = new HashMap<>();
                                addressComponents.put("house_number", address.path("house_number").asText(""));
                                addressComponents.put("road", address.path("road").asText(""));
                                addressComponents.put("postcode", address.path("postcode").asText(""));
                                addressComponents.put("city", address.path("city").asText(address.path("town").asText(address.path("village").asText(""))));
                                suggestion.put("address_components", addressComponents);
                            }

                            suggestion.put("type", result.path("type").asText());
                            suggestion.put("importance", result.path("importance").asDouble(0.0));
                            suggestions.add(suggestion);
                        }
                    }
                    return suggestions;
                })
                .onErrorResume(e -> {
                    return Mono.just(new ArrayList<>());
                });
    }

    public Mono<Optional<Coordinates>> geocodeAddress(String address, String postalCode, String city) {
        StringBuilder queryBuilder = new StringBuilder(address);
        if (city != null && !city.isEmpty() && !address.toLowerCase().contains(city.toLowerCase())) {
            queryBuilder.append(", ").append(city);
        }
        if (postalCode != null && !postalCode.isEmpty() && !address.contains(postalCode)) {
            queryBuilder.append(" ").append(postalCode);
        }
        final String finalQuery = queryBuilder.toString().trim();

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", finalQuery)
                        .queryParam("format", "jsonv2")
                        .queryParam("addressdetails", 1)
                        .queryParam("limit", 1)
                        .queryParam("countrycodes", "fr")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(responseArray -> {
                    if (responseArray.isArray() && !responseArray.isEmpty()) {
                        JsonNode bestMatch = responseArray.get(0);
                        double lat = bestMatch.path("lat").asDouble();
                        double lon = bestMatch.path("lon").asDouble();
                        if (lat != 0.0 && lon != 0.0) {
                            return Optional.of(new Coordinates(lat, lon));
                        }
                    }
                    return Optional.<Coordinates>empty();
                })
                .onErrorResume(e -> {
                    return Mono.just(Optional.empty());
                });
    }
}
