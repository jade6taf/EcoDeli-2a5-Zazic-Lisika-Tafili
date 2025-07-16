package com.ecodeli.analytics.services;

import com.ecodeli.analytics.dto.LivraisonDto;
import com.ecodeli.analytics.dto.UserDto;
import com.ecodeli.analytics.dto.ServiceDto;
import com.ecodeli.analytics.dto.ClientAnalyticsDto;
import com.ecodeli.analytics.dto.PrestationAnalyticsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackendApiService {

    private final RestTemplate restTemplate;

    @Value("${ecodeli.backend.url}")
    private String backendUrl;

    @Value("${ecodeli.auth.admin.email}")
    private String adminEmail;

    @Value("${ecodeli.auth.admin.password}")
    private String adminPassword;

    private String authToken;

    public boolean authenticate() {
        try {
            String loginUrl = backendUrl + "/api/auth/login";

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", adminEmail);
            loginRequest.put("motDePasse", adminPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("token")) {
                    this.authToken = (String) responseBody.get("token");
                } else if (responseBody.containsKey("accessToken")) {
                    this.authToken = (String) responseBody.get("accessToken");
                } else if (responseBody.containsKey("jwt")) {
                    this.authToken = (String) responseBody.get("jwt");
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Erreur d'authentification: {}", e.getMessage());
        }
        return false;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }
        return headers;
    }

    public List<UserDto> getAllUsers() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/users";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<UserDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
        }

        return List.of();
    }

    public List<UserDto> getUsersByType(String type) {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/users?type=" + type;
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<UserDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs type {}: {}", type, e.getMessage());
        }

        return List.of();
    }

    public List<LivraisonDto> getAllLivraisons() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/livraisons";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<LivraisonDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<LivraisonDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des livraisons: {}", e.getMessage());
        }

        return List.of();
    }

    public Map<String, Object> getUserStats() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/users/stats";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stats utilisateurs: {}", e.getMessage());
        }

        return Map.of();
    }

    public Map<String, Object> getLivraisonStats() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/livraisons/stats";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stats livraisons: {}", e.getMessage());
        }

        return Map.of();
    }

    public List<ClientAnalyticsDto> getAllClientsWithInvoices() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/clients/analytics";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<ClientAnalyticsDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ClientAnalyticsDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des clients analytics: {}", e.getMessage());
        }

        return List.of();
    }

    public List<PrestationAnalyticsDto> getAllPrestations() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/prestations/analytics";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<PrestationAnalyticsDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<PrestationAnalyticsDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des prestations analytics: {}", e.getMessage());
        }

        return List.of();
    }

    public List<ServiceDto> getAllServices() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            String url = backendUrl + "/api/admin/services";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<ServiceDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ServiceDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des services: {}", e.getMessage());
        }

        return List.of();
    }

    public Map<String, Object> getCompleteAnalyticsData() {
        try {
            if (authToken == null && !authenticate()) {
                throw new RuntimeException("Impossible de s'authentifier");
            }

            Map<String, Object> data = new HashMap<>();

            List<UserDto> users = getAllUsers();
            List<LivraisonDto> livraisons = getAllLivraisons();
            List<ServiceDto> services = getAllServices();
            List<ClientAnalyticsDto> clients = getAllClientsWithInvoices();
            List<PrestationAnalyticsDto> prestations = getAllPrestations();

            data.put("users", users);
            data.put("livraisons", livraisons);
            data.put("services", services);
            data.put("clients", clients);
            data.put("prestations", prestations);
            data.put("userStats", getUserStats());
            data.put("livraisonStats", getLivraisonStats());
            data.put("totalUsers", users.size());
            data.put("totalLivraisons", livraisons.size());
            data.put("totalServices", services.size());
            data.put("totalClients", clients.size());
            data.put("totalPrestations", prestations.size());
            data.put("dataSource", "BACKEND");
            data.put("retrievalTime", java.time.LocalDateTime.now());

            return data;

        } catch (Exception e) {
            return Map.of();
        }
    }

    public boolean isAnalyticsEndpointAvailable() {
        try {
            if (authToken == null && !authenticate()) {
                return false;
            }

            String url = backendUrl + "/api/admin/analytics/health";
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean testConnectivity() {
        try {
            String url = backendUrl + "/api/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
