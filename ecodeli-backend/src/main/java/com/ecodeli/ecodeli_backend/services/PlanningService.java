package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.DisponibiliteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanningService {

    @Autowired
    private DisponibiliteRepository disponibiliteRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DisponibilitePrestataire creerDisponibilite(Integer prestataireId, DisponibiliteRequest request) {

        if (!request.isValid()) {
            throw new IllegalArgumentException(request.getValidationError());
        }

        Prestataire prestataire = getPrestataire(prestataireId);

        verifierConflits(prestataireId, request.getDateDebut(), request.getDateFin(), null);

        DisponibilitePrestataire disponibilite = new DisponibilitePrestataire();
        disponibilite.setId(null);
        mapRequestToEntity(request, disponibilite);
        disponibilite.setPrestataire(prestataire);

        if (Boolean.TRUE.equals(request.getRecurrent()) && request.getJoursRecurrence() != null) {
            disponibilite.setJoursRecurrence(convertListToJson(request.getJoursRecurrence()));
        }

        return disponibiliteRepository.save(disponibilite);
    }

    public DisponibilitePrestataire modifierDisponibilite(Integer prestataireId, Long disponibiliteId, DisponibiliteRequest request) {

        if (!request.isValid()) {
            throw new IllegalArgumentException(request.getValidationError());
        }

        DisponibilitePrestataire disponibilite = disponibiliteRepository.findById(disponibiliteId)
                .orElseThrow(() -> new RuntimeException("Disponibilité non trouvée"));

        if (!disponibilite.getPrestataire().getIdUtilisateur().equals(prestataireId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette disponibilité");
        }

        verifierConflits(prestataireId, request.getDateDebut(), request.getDateFin(), disponibiliteId);

        mapRequestToEntity(request, disponibilite);

        if (Boolean.TRUE.equals(request.getRecurrent()) && request.getJoursRecurrence() != null) {
            disponibilite.setJoursRecurrence(convertListToJson(request.getJoursRecurrence()));
        } else {
            disponibilite.setJoursRecurrence(null);
        }

        return disponibiliteRepository.save(disponibilite);
    }

    public void supprimerDisponibilite(Integer prestataireId, Long disponibiliteId) {
        DisponibilitePrestataire disponibilite = disponibiliteRepository.findById(disponibiliteId)
                .orElseThrow(() -> new RuntimeException("Disponibilité non trouvée"));

        if (!disponibilite.getPrestataire().getIdUtilisateur().equals(prestataireId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette disponibilité");
        }

        disponibiliteRepository.delete(disponibilite);
    }

    public List<Map<String, Object>> getDisponibilitesForCalendar(Integer prestataireId, LocalDate debut, LocalDate fin) {
        LocalDateTime dateDebut = debut.atStartOfDay();
        LocalDateTime dateFin = fin.plusDays(1).atStartOfDay();

        List<DisponibilitePrestataire> disponibilites = disponibiliteRepository
                .findDisponibilitesInRange(prestataireId, StatutDisponibilite.ACTIVE, dateDebut, dateFin);

        return disponibilites.stream()
                .map(this::convertToFullCalendarEvent)
                .collect(Collectors.toList());
    }

    public PlanningStatsResponse getStatistiques(Integer prestataireId) {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutSemaine = maintenant.toLocalDate().atStartOfDay();
        LocalDateTime finSemaine = debutSemaine.plusDays(7);
        LocalDateTime debutMois = maintenant.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = debutMois.plusMonths(1);

        List<DisponibilitePrestataire> prochains = disponibiliteRepository
                .findUpcomingDisponibilites(prestataireId, StatutDisponibilite.ACTIVE, maintenant);

        Long heuresDisponiblesSemaine = disponibiliteRepository
                .countHeuresDisponibles(prestataireId, TypeDisponibilite.DISPONIBLE, StatutDisponibilite.ACTIVE, debutSemaine, finSemaine);

        Long heuresOccupeesSemaine = disponibiliteRepository
                .countHeuresDisponibles(prestataireId, TypeDisponibilite.OCCUPE, StatutDisponibilite.ACTIVE, debutSemaine, finSemaine);

        Long heuresDisponiblesMois = disponibiliteRepository
                .countHeuresDisponibles(prestataireId, TypeDisponibilite.DISPONIBLE, StatutDisponibilite.ACTIVE, debutMois, finMois);

        PlanningStatsResponse stats = PlanningStatsResponse.builder()
                .prochainsCreneaux(Math.min(prochains.size(), 10)) // Limiter à 10
                .heuresDisponibles(heuresDisponiblesSemaine != null ? heuresDisponiblesSemaine : 0L)
                .heuresOccupees(heuresOccupeesSemaine != null ? heuresOccupeesSemaine : 0L)
                .prochainCreneau(prochains.isEmpty() ? null : prochains.get(0).getDateDebut())
                .heuresDisponiblesSemaine(heuresDisponiblesSemaine != null ? heuresDisponiblesSemaine : 0L)
                .heuresDisponiblesMois(heuresDisponiblesMois != null ? heuresDisponiblesMois : 0L)
                .build();

        stats.calculateTauxOccupation();
        return stats;
    }

    public List<DisponibilitePrestataire> getProchainsCreneaux(Integer prestataireId, int limite) {
        List<DisponibilitePrestataire> prochains = disponibiliteRepository
                .findUpcomingDisponibilites(prestataireId, StatutDisponibilite.ACTIVE, LocalDateTime.now());

        return prochains.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<DisponibilitePrestataire> creerCreneauxRecurrents(Integer prestataireId, DisponibiliteRequest request) {
        if (!Boolean.TRUE.equals(request.getRecurrent()) || request.getJoursRecurrence() == null) {
            throw new IllegalArgumentException("Les paramètres de récurrence sont requis");
        }

        List<DisponibilitePrestataire> creneaux = new ArrayList<>();
        LocalDate finRecurrence = request.getFinRecurrence() != null ?
                request.getFinRecurrence() :
                request.getDateDebut().toLocalDate().plusMonths(3);

        Set<DayOfWeek> joursSelectionnes = request.getJoursRecurrence().stream()
                .map(this::convertStringToDayOfWeek)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        LocalDate dateActuelle = request.getDateDebut().toLocalDate();
        while (!dateActuelle.isAfter(finRecurrence)) {
            if (joursSelectionnes.contains(dateActuelle.getDayOfWeek())) {
                LocalDateTime debutCreneau = dateActuelle.atTime(request.getDateDebut().toLocalTime());
                LocalDateTime finCreneau = dateActuelle.atTime(request.getDateFin().toLocalTime());

                try {
                    DisponibiliteRequest creneauRequest = new DisponibiliteRequest();
                    mapRequestToRequest(request, creneauRequest);
                    creneauRequest.setDateDebut(debutCreneau);
                    creneauRequest.setDateFin(finCreneau);
                    creneauRequest.setRecurrent(false);

                    DisponibilitePrestataire creneau = creerDisponibilite(prestataireId, creneauRequest);
                    creneaux.add(creneau);
                } catch (Exception e) {
                }
            }
            dateActuelle = dateActuelle.plusDays(1);
        }

        return creneaux;
    }

    private Prestataire getPrestataire(Integer prestataireId) {
        return utilisateurService.getUtilisateurById(prestataireId)
                .filter(u -> u instanceof Prestataire)
                .map(u -> (Prestataire) u)
                .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
    }

    private void verifierConflits(Integer prestataireId, LocalDateTime dateDebut, LocalDateTime dateFin, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        List<DisponibilitePrestataire> conflits = disponibiliteRepository
                .findConflictingDisponibilites(prestataireId, StatutDisponibilite.ACTIVE, dateDebut, dateFin, excludeIdSafe);

        if (!conflits.isEmpty()) {
            throw new IllegalArgumentException("Ce créneau entre en conflit avec une disponibilité existante");
        }
    }

    private void mapRequestToEntity(DisponibiliteRequest request, DisponibilitePrestataire entity) {
        entity.setDateDebut(request.getDateDebut());
        entity.setDateFin(request.getDateFin());
        entity.setType(request.getType());
        entity.setStatut(request.getStatut() != null ? request.getStatut() : StatutDisponibilite.ACTIVE);
        entity.setTitre(request.getTitre());
        entity.setDescription(request.getDescription());
        entity.setRecurrent(request.getRecurrent());
        entity.setFinRecurrence(request.getFinRecurrence());
    }

    private void mapRequestToRequest(DisponibiliteRequest source, DisponibiliteRequest target) {
        target.setType(source.getType());
        target.setStatut(source.getStatut());
        target.setTitre(source.getTitre());
        target.setDescription(source.getDescription());
        target.setJoursRecurrence(source.getJoursRecurrence());
        target.setFinRecurrence(source.getFinRecurrence());
    }

    private Map<String, Object> convertToFullCalendarEvent(DisponibilitePrestataire dispo) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", dispo.getId());
        event.put("title", dispo.getTitre() != null ? dispo.getTitre() : dispo.getLibelleType());
        event.put("start", dispo.getDateDebut().toString());
        event.put("end", dispo.getDateFin().toString());
        event.put("backgroundColor", dispo.getCouleur());
        event.put("borderColor", dispo.getCouleur());
        event.put("textColor", "#ffffff");

        Map<String, Object> extendedProps = new HashMap<>();
        extendedProps.put("type", dispo.getType());
        extendedProps.put("description", dispo.getDescription());
        extendedProps.put("recurrent", dispo.getRecurrent());
        event.put("extendedProps", extendedProps);

        return event;
    }

    private String convertListToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private DayOfWeek convertStringToDayOfWeek(String jour) {
        switch (jour.toUpperCase()) {
            case "LUNDI": return DayOfWeek.MONDAY;
            case "MARDI": return DayOfWeek.TUESDAY;
            case "MERCREDI": return DayOfWeek.WEDNESDAY;
            case "JEUDI": return DayOfWeek.THURSDAY;
            case "VENDREDI": return DayOfWeek.FRIDAY;
            case "SAMEDI": return DayOfWeek.SATURDAY;
            case "DIMANCHE": return DayOfWeek.SUNDAY;
            default: return null;
        }
    }
}
