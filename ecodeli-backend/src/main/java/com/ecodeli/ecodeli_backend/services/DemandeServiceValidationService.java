package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DemandeServiceValidationService {

    @Autowired
    private Validator validator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> validateDetailsSpecifiques(ServiceType serviceType, Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return List.of("Les détails spécifiques sont obligatoires pour ce type de service");
        }

        try {
            switch (serviceType) {
                case TRANSPORT_LIVRAISON:
                    return validateTransportDetails(details);
                case SERVICES_DOMICILE:
                    return validateServiceDomicileDetails(details);
                case TRAVAUX_REPARATIONS:
                    return validateTravauxDetails(details);
                case COURSES_ACHATS:
                    return validateCoursesDetails(details);
                case SERVICES_PERSONNELS:
                    return validateServicesPersonnelsDetails(details);
                case EDUCATION_FORMATION:
                    return validateEducationDetails(details);
                default:
                    return List.of("Type de service non reconnu");
            }
        } catch (Exception e) {
            return List.of("Erreur lors de la validation des détails spécifiques: " + e.getMessage());
        }
    }

    private List<String> validateTransportDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            TransportDetailsDTO dto = objectMapper.readValue(json, TransportDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données transport invalide");
        }
    }

    private List<String> validateServiceDomicileDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            ServiceDomicileDetailsDTO dto = objectMapper.readValue(json, ServiceDomicileDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données service à domicile invalide");
        }
    }

    private List<String> validateTravauxDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            TravauxDetailsDTO dto = objectMapper.readValue(json, TravauxDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données travaux invalide");
        }
    }

    private List<String> validateCoursesDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            CoursesDetailsDTO dto = objectMapper.readValue(json, CoursesDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données courses invalide");
        }
    }

    private List<String> validateServicesPersonnelsDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            ServicesPersonnelsDetailsDTO dto = objectMapper.readValue(json, ServicesPersonnelsDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données services personnels invalide");
        }
    }

    private List<String> validateEducationDetails(Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            EducationDetailsDTO dto = objectMapper.readValue(json, EducationDetailsDTO.class);
            return validateDTO(dto);
        } catch (JsonProcessingException e) {
            return List.of("Format des données éducation invalide");
        }
    }

    private <T> List<String> validateDTO(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        List<String> errors = new ArrayList<>();

        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getMessage());
        }
        return errors;
    }

    public List<String> validateCoherence(DemandeService demande) {
        List<String> errors = new ArrayList<>();

        if (demande.getCategorieService() == null) {
            errors.add("La catégorie de service est obligatoire");
            return errors;
        }

        Map<String, Object> details = demande.getDetailsSpecifiquesAsMap();

        switch (demande.getCategorieService()) {
            case TRANSPORT_LIVRAISON:
                validateTransportCoherence(demande, details, errors);
                break;
            case SERVICES_DOMICILE:
                validateServiceDomicileCoherence(demande, details, errors);
                break;
            // Ajouter d'autres validations de cohérence si nécessaire
        }
        return errors;
    }

    private void validateTransportCoherence(DemandeService demande, Map<String, Object> details, List<String> errors) {
        String lieuDepart = (String) details.get("lieuDepart");
        String lieuArrivee = (String) details.get("lieuArrivee");

        if (demande.getAdresseDepart() != null && lieuDepart != null &&
            !demande.getAdresseDepart().equals(lieuDepart)) {
            errors.add("Incohérence entre l'adresse de départ générique et celle des détails");
        }
        if (demande.getAdresseArrivee() != null && lieuArrivee != null &&
            !demande.getAdresseArrivee().equals(lieuArrivee)) {
            errors.add("Incohérence entre l'adresse d'arrivée générique et celle des détails");
        }
    }

    private void validateServiceDomicileCoherence(DemandeService demande, Map<String, Object> details, List<String> errors) {
        // Validation spécifique pour les services à domicile (adresse de service est cohérente)
    }
}
