package com.ecodeli.analytics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGeneratorService {

    private static final Color BLUE_CORPORATE = new Color(41, 98, 255);
    private static final Color ORANGE_MODERN = new Color(255, 111, 0);
    private static final Color PURPLE_ELEGANT = new Color(124, 77, 255);
    private static final Color TEAL_PROFESSIONAL = new Color(0, 137, 123);
    private static final Color RED_ACCENT = new Color(244, 67, 54);
    private static final Color GREEN_ECODELI = new Color(46, 125, 50);
    private static final Color AMBER_WARM = new Color(255, 193, 7);
    private static final Color PINK_MODERN = new Color(236, 64, 122);
    private static final Color CYAN_VIBRANT = new Color(0, 188, 212);
    private static final Color BACKGROUND_WHITE = new Color(255, 255, 255);
    private static final Color BACKGROUND_LIGHT = new Color(248, 249, 250);
    private static final Color BORDER_DARK = new Color(33, 37, 41);


    public JFreeChart createUserDistributionPieChart(Map<String, Object> data) {

        DefaultPieDataset dataset = new DefaultPieDataset();

        List<Map<String, Object>> clients = (List<Map<String, Object>>) data.get("clients");
        List<Map<String, Object>> commercants = (List<Map<String, Object>>) data.get("commercants");
        List<Map<String, Object>> livreurs = (List<Map<String, Object>>) data.get("livreurs");

        dataset.setValue("Clients", clients != null ? clients.size() : 0);
        dataset.setValue("Commerçants", commercants != null ? commercants.size() : 0);
        dataset.setValue("Livreurs", livreurs != null ? livreurs.size() : 0);

        JFreeChart chart = ChartFactory.createPieChart(
            "Répartition des Utilisateurs par Type",
            dataset,
            true,
            true,
            false
        );
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Clients", BLUE_CORPORATE);
        plot.setSectionPaint("Commerçants", ORANGE_MODERN);
        plot.setSectionPaint("Livreurs", PURPLE_ELEGANT);
        plot.setBackgroundPaint(BACKGROUND_WHITE);
        plot.setOutlinePaint(BORDER_DARK);
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));
        plot.setLabelBackgroundPaint(BACKGROUND_LIGHT);
        return chart;
    }

    public JFreeChart createDeliveryStatusBarChart(Map<String, Object> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        if (livraisons != null) {
            long enCours = livraisons.stream().filter(l -> "EN_COURS".equals(l.get("statut"))).count();
            long terminees = livraisons.stream().filter(l -> "TERMINEE".equals(l.get("statut"))).count();
            long annulees = livraisons.stream().filter(l -> "ANNULEE".equals(l.get("statut"))).count();
            long attente = livraisons.stream().filter(l -> "ATTENTE_SEGMENT_2".equals(l.get("statut"))).count();

            dataset.addValue(enCours, "Livraisons", "En Cours");
            dataset.addValue(terminees, "Livraisons", "Terminées");
            dataset.addValue(annulees, "Livraisons", "Annulées");
            dataset.addValue(attente, "Livraisons", "En Attente");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Répartition des Livraisons par Statut",
            "Statut",
            "Nombre de Livraisons",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, BLUE_CORPORATE);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(1, ORANGE_MODERN);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(2, PURPLE_ELEGANT);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(3, TEAL_PROFESSIONAL);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }

    public JFreeChart createRevenueEvolutionChart(Map<String, Object> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun"};

        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        List<Map<String, Object>> services = (List<Map<String, Object>>) data.get("services");

        double revenusLivraisons = livraisons != null ?
            livraisons.stream().mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0)).sum() : 0;
        double revenusServices = services != null ?
            services.stream().mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0)).sum() : 0;

        for (int i = 0; i < mois.length; i++) {
            double facteur = 0.8 + (Math.random() * 0.4);
            dataset.addValue(revenusLivraisons * facteur / 6, "Livraisons", mois[i]);
            dataset.addValue(revenusServices * facteur / 6, "Services", mois[i]);
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Évolution des Revenus par Mois",
            "Mois",
            "Revenus (€)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, GREEN_ECODELI);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(1, AMBER_WARM);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }

    public JFreeChart createPerformanceAreaChart(Map<String, Object> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] periodes = {"Sem 1", "Sem 2", "Sem 3", "Sem 4"};

        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        List<Map<String, Object>> services = (List<Map<String, Object>>) data.get("services");

        int totalUsers = users != null ? users.size() : 0;
        int totalLivraisons = livraisons != null ? livraisons.size() : 0;
        int totalServices = services != null ? services.size() : 0;

        for (int i = 0; i < periodes.length; i++) {
            double facteur = 0.6 + (i * 0.1) + (Math.random() * 0.2);
            dataset.addValue(totalUsers * facteur / 4, "Nouveaux Utilisateurs", periodes[i]);
            dataset.addValue(totalLivraisons * facteur / 4, "Livraisons Effectuées", periodes[i]);
            dataset.addValue(totalServices * facteur / 4, "Services Rendus", periodes[i]);
        }

        JFreeChart chart = ChartFactory.createAreaChart(
            "Performances Globales par Semaine",
            "Période",
            "Volume d'Activité",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, GREEN_ECODELI);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(1, TEAL_PROFESSIONAL);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(2, AMBER_WARM);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }

    public JFreeChart createClientRevenueDistributionChart(Map<String, Object> data) {

        DefaultPieDataset dataset = new DefaultPieDataset();

        List<Map<String, Object>> clients = (List<Map<String, Object>>) data.get("clients");

        int tranche0_100 = 0;
        int tranche101_500 = 0;
        int tranche500Plus = 0;

        if (clients != null) {
            for (Map<String, Object> client : clients) {
                Double ca = (Double) client.getOrDefault("totalDepense", 0.0);
                if (ca <= 100) {
                    tranche0_100++;
                } else if (ca <= 500) {
                    tranche101_500++;
                } else {
                    tranche500Plus++;
                }
            }
        }

        dataset.setValue("0-100€ (" + tranche0_100 + " clients)", tranche0_100);
        dataset.setValue("101-500€ (" + tranche101_500 + " clients)", tranche101_500);
        dataset.setValue(">500€ (" + tranche500Plus + " clients)", tranche500Plus);

        JFreeChart chart = ChartFactory.createPieChart(
            "Répartition des Clients par Chiffre d'Affaires",
            dataset,
            true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("0-100€ (" + tranche0_100 + " clients)", GREEN_ECODELI);
        plot.setSectionPaint("101-500€ (" + tranche101_500 + " clients)", TEAL_PROFESSIONAL);
        plot.setSectionPaint(">500€ (" + tranche500Plus + " clients)", BLUE_CORPORATE);
        plot.setBackgroundPaint(BACKGROUND_WHITE);
        plot.setOutlinePaint(BORDER_DARK);
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));
        plot.setLabelBackgroundPaint(BACKGROUND_LIGHT);
        return chart;
    }

    public JFreeChart createTopLoyalClientsChart(Map<String, Object> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Map<String, Object>> clients = (List<Map<String, Object>>) data.get("clients");

        if (clients != null && !clients.isEmpty()) {
            clients.sort((c1, c2) -> {
                Integer cmd1 = (Integer) c1.getOrDefault("nbCommandes", 0);
                Integer cmd2 = (Integer) c2.getOrDefault("nbCommandes", 0);
                return cmd2.compareTo(cmd1);
            });

            for (int i = 0; i < Math.min(5, clients.size()); i++) {
                Map<String, Object> client = clients.get(i);
                String nom = (String) client.get("prenom") + " " + (String) client.get("nom");
                Integer score = (Integer) client.getOrDefault("nbCommandes", 0);
                dataset.addValue(score, "Score Fidélité", nom);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top 5 des Clients les Plus Fidèles",
            "Clients",
            "Nombre de Commandes",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, GREEN_ECODELI);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }

    public JFreeChart createServiceTypeDistributionChart(Map<String, Object> data) {

        DefaultPieDataset dataset = new DefaultPieDataset();

        dataset.setValue("🚚 Transport & Livraison", 15);
        dataset.setValue("🏠 Services à Domicile", 12);
        dataset.setValue("🔧 Travaux & Réparations", 8);
        dataset.setValue("🛒 Courses & Achats", 10);
        dataset.setValue("👥 Services Personnels", 6);
        dataset.setValue("🎓 Éducation & Formation", 4);

        JFreeChart chart = ChartFactory.createPieChart(
            "Répartition par Type de Prestation",
            dataset,
            true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("🚚 Transport & Livraison", GREEN_ECODELI);
        plot.setSectionPaint("🏠 Services à Domicile", TEAL_PROFESSIONAL);
        plot.setSectionPaint("🔧 Travaux & Réparations", ORANGE_MODERN);
        plot.setSectionPaint("🛒 Courses & Achats", AMBER_WARM);
        plot.setSectionPaint("👥 Services Personnels", PURPLE_ELEGANT);
        plot.setSectionPaint("🎓 Éducation & Formation", BLUE_CORPORATE);
        plot.setBackgroundPaint(BACKGROUND_WHITE);
        plot.setOutlinePaint(BORDER_DARK);
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));
        plot.setLabelBackgroundPaint(BACKGROUND_LIGHT);

        return chart;
    }

    public JFreeChart createServiceFrequencyChart(Map<String, Object> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(45, "Utilisations", "Transport");
        dataset.addValue(32, "Utilisations", "Services Domicile");
        dataset.addValue(28, "Utilisations", "Travaux");
        dataset.addValue(35, "Utilisations", "Courses");
        dataset.addValue(18, "Utilisations", "Services Perso");
        dataset.addValue(12, "Utilisations", "Formation");

        JFreeChart chart = ChartFactory.createBarChart(
            "Fréquence d'Utilisation des Prestations (par mois)",
            "Types de Prestations",
            "Nombre d'Utilisations",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, AMBER_WARM);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }


    public JFreeChart createTopPopularServicesChart(Map<String, Object> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(85, "Demandes", "Livraison Express");
        dataset.addValue(72, "Demandes", "Ménage à Domicile");
        dataset.addValue(58, "Demandes", "Courses Alimentaires");
        dataset.addValue(45, "Demandes", "Bricolage");
        dataset.addValue(38, "Demandes", "Garde d'Enfants");

        JFreeChart chart = ChartFactory.createBarChart(
            "Top 5 des Prestations les Plus Demandées",
            "Prestations",
            "Nombre de Demandes",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, TEAL_PROFESSIONAL);
        chart.setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setBackgroundPaint(BACKGROUND_WHITE);
        chart.getCategoryPlot().setOutlinePaint(BORDER_DARK);
        chart.getCategoryPlot().setDomainGridlinePaint(BACKGROUND_LIGHT);
        chart.getCategoryPlot().setRangeGridlinePaint(BACKGROUND_LIGHT);

        return chart;
    }


    public Map<String, JFreeChart> generateAllCharts(Map<String, Object> data) {

        Map<String, JFreeChart> charts = Map.of(
            "userDistribution", createUserDistributionPieChart(data),
            "deliveryStatus", createDeliveryStatusBarChart(data),
            "revenueEvolution", createRevenueEvolutionChart(data),
            "performance", createPerformanceAreaChart(data),
            "clientRevenueDistribution", createClientRevenueDistributionChart(data),
            "topLoyalClients", createTopLoyalClientsChart(data),
            "serviceTypeDistribution", createServiceTypeDistributionChart(data),
            "serviceFrequency", createServiceFrequencyChart(data),
            "topPopularServices", createTopPopularServicesChart(data)
        );

        return charts;
    }
}
