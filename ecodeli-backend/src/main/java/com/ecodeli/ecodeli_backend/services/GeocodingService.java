package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

@Service
public class GeocodingService {

    private WebClient webClient;

    @Value("${nominatim.api.url:https://nominatim.openstreetmap.org}")
    private String nominatimApiUrl;

    @Value("${application.name:EcodeliBackend}") // Pour le User-Agent
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


    /**
     * Géocode une adresse en utilisant l'API Nominatim.
     *
     * @param address L'adresse complète (rue, numéro, ville, code postal si possible).
     *                Nominatim est plus efficace avec une adresse complète dans le paramètre 'q'
     *                ou des paramètres structurés.
     * @param postalCode Le code postal (optionnel, peut être inclus dans address).
     * @param city La ville (optionnel, peut être inclus dans address).
     * @return Un Optional contenant les Coordinates si trouvées, sinon Optional.empty().
     */
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