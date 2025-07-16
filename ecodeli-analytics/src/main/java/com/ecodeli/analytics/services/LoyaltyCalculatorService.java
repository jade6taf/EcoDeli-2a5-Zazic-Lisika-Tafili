package com.ecodeli.analytics.services;

import com.ecodeli.analytics.dto.ClientAnalyticsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoyaltyCalculatorService {

    private static final double COEFF_COMMANDES = 0.4;
    private static final double COEFF_FREQUENCE = 0.3;
    private static final double COEFF_ANCIENNETE = 0.3;

    private static final int MAX_COMMANDES_REF = 50;
    private static final int MAX_FREQUENCE_REF = 10;
    private static final int MAX_ANCIENNETE_REF = 365;

    public Double calculateLoyaltyScore(ClientAnalyticsDto client) {
        try {
            if (client.getNombreCommandes() == null || client.getNombreCommandes() <= 0) {
                return 0.0;
            }

            double scoreCommandes = Math.min(100.0,
                (client.getNombreCommandes().doubleValue() / MAX_COMMANDES_REF) * 100.0);

            double scoreFrequence = 0.0;
            if (client.getFrequenceCommandes() != null && client.getFrequenceCommandes() > 0) {
                scoreFrequence = Math.min(100.0,
                    (client.getFrequenceCommandes().doubleValue() / MAX_FREQUENCE_REF) * 100.0);
            }

           double scoreAnciennete = 0.0;
            if (client.getAncienneteJours() != null && client.getAncienneteJours() > 0) {
                scoreAnciennete = Math.min(100.0,
                    (client.getAncienneteJours().doubleValue() / MAX_ANCIENNETE_REF) * 100.0);
            } else if (client.getDatePremiereCommande() != null) {
                long jours = ChronoUnit.DAYS.between(client.getDatePremiereCommande(), LocalDateTime.now());
                client.setAncienneteJours((int) jours);
                scoreAnciennete = Math.min(100.0, (jours / (double) MAX_ANCIENNETE_REF) * 100.0);
            }

            double scoreFinal = (scoreCommandes * COEFF_COMMANDES) +
                               (scoreFrequence * COEFF_FREQUENCE) +
                               (scoreAnciennete * COEFF_ANCIENNETE);

            if (Boolean.TRUE.equals(client.getIsActive())) {
                scoreFinal = Math.min(100.0, scoreFinal * 1.1);
            }

            return Math.round(scoreFinal * 100.0) / 100.0;

        } catch (Exception e) {
            return 0.0;
        }
    }


    public Integer calculateFrequencyPerMonth(ClientAnalyticsDto client) {
        if (client.getNombreCommandes() == null || client.getNombreCommandes() <= 0) {
            return 0;
        }

        if (client.getDatePremiereCommande() == null) {
            return client.getNombreCommandes();
        }

        try {
            LocalDateTime dateDebut = client.getDatePremiereCommande();
            LocalDateTime dateFin = client.getDateDerniereCommande() != null ?
                client.getDateDerniereCommande() : LocalDateTime.now();

            long mois = ChronoUnit.MONTHS.between(dateDebut, dateFin);
            if (mois <= 0) mois = 1;

            int frequence = (int) Math.ceil(client.getNombreCommandes().doubleValue() / mois);
            return frequence;

        } catch (Exception e) {
            return client.getNombreCommandes();
        }
    }

    public List<ClientAnalyticsDto> getTopLoyalClients(List<ClientAnalyticsDto> clients, int topN) {

        clients.forEach(client -> {
            if (client.getFrequenceCommandes() == null) {
                client.setFrequenceCommandes(calculateFrequencyPerMonth(client));
            }
            if (client.getScoreFidelite() == null) {
                client.setScoreFidelite(calculateLoyaltyScore(client));
            }
        });

        List<ClientAnalyticsDto> topClients = clients.stream()
            .filter(client -> client.getScoreFidelite() != null && client.getScoreFidelite() > 0)
            .sorted((c1, c2) -> Double.compare(c2.getScoreFidelite(), c1.getScoreFidelite()))
            .limit(topN)
            .collect(Collectors.toList());

        return topClients;
    }

    public void updateLoyaltyMetrics(List<ClientAnalyticsDto> clients) {
        for (ClientAnalyticsDto client : clients) {
            if (client.getFrequenceCommandes() == null) {
                client.setFrequenceCommandes(calculateFrequencyPerMonth(client));
            }

            if (client.getScoreFidelite() == null) {
                client.setScoreFidelite(calculateLoyaltyScore(client));
            }

            if (client.getAncienneteJours() == null && client.getDatePremiereCommande() != null) {
                long jours = ChronoUnit.DAYS.between(client.getDatePremiereCommande(), LocalDateTime.now());
                client.setAncienneteJours((int) jours);
            }

            if (client.getIsActive() == null) {
                boolean isActive = client.getDateDerniereCommande() != null &&
                    ChronoUnit.DAYS.between(client.getDateDerniereCommande(), LocalDateTime.now()) <= 90;
                client.setIsActive(isActive);
            }
        }
    }


    public String generateLoyaltyStats(List<ClientAnalyticsDto> clients) {
        if (clients.isEmpty()) return "Aucun client à analyser";

        double moyenneScore = clients.stream()
            .filter(c -> c.getScoreFidelite() != null)
            .mapToDouble(ClientAnalyticsDto::getScoreFidelite)
            .average()
            .orElse(0.0);

        long clientsFideles = clients.stream()
            .filter(ClientAnalyticsDto::isClientFidele)
            .count();

        long clientsActifs = clients.stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .count();

        return String.format(
            "📊 Statistiques Fidélité: Score moyen: %.1f/100 | Clients fidèles: %d/%d (%.1f%%) | Clients actifs: %d/%d (%.1f%%)",
            moyenneScore, clientsFideles, clients.size(),
            (clientsFideles * 100.0 / clients.size()),
            clientsActifs, clients.size(),
            (clientsActifs * 100.0 / clients.size())
        );
    }
}
