package com.ecodeli.analytics.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private final ChartGeneratorService chartGeneratorService;
    private final DataGeneratorService dataGeneratorService;

    @Value("${analytics.reports.output-directory}")
    private String outputDirectory;

    @Value("${analytics.reports.filename-pattern}")
    private String filenamePattern;

    public String generatePdfReport(Map<String, Object> data, Map<String, org.jfree.chart.JFreeChart> charts) throws IOException {

        String filename = String.format(filenamePattern,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        Path outputPath = Paths.get(outputDirectory, filename);

        Files.createDirectories(outputPath.getParent());

        try (PdfWriter writer = new PdfWriter(outputPath.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {


            addTitlePage(document, data);
            addExecutiveSummary(document, data);
            for (Map.Entry<String, org.jfree.chart.JFreeChart> entry : charts.entrySet()) {
                String title = getChartTitle(entry.getKey());
                addSection(document, title, entry.getValue(), data, entry.getKey());
            }
            addConclusions(document, data);

            return outputPath.toString();
        }
    }


    public String generateCompleteReport(Map<String, Object> data) throws IOException {

        boolean needGeneratedData = needDataGeneration(data);
        Map<String, Object> finalData = data;

        if (needGeneratedData) {
            Map<String, Object> generatedData = dataGeneratorService.generateCompleteDataset();
            finalData = mergeData(data, generatedData);
        }

        Map<String, JFreeChart> charts = chartGeneratorService.generateAllCharts(finalData);

        String filename = String.format(filenamePattern,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        Path outputPath = Paths.get(outputDirectory, filename);

        Files.createDirectories(outputPath.getParent());

        try (PdfWriter writer = new PdfWriter(outputPath.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addTitlePage(document, finalData);
            addExecutiveSummary(document, finalData);
            document.add(new AreaBreak());

            Paragraph page1Title = new Paragraph("PAGE 1 - ANALYSE CLIENTS ET COMMERÇANTS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(page1Title);

            addSection(document, "1. Répartition des Utilisateurs par Type",
                      charts.get("userDistribution"), finalData, "userDistribution");

            addSection(document, "2. Répartition des Clients par Chiffre d'Affaires",
                      charts.get("clientRevenueDistribution"), finalData, "clientRevenueDistribution");

            addSection(document, "3. Évolution des Revenus Mensuels",
                      charts.get("revenueEvolution"), finalData, "revenueEvolution");

            addSection(document, "4. Top 5 des Clients les Plus Fidèles",
                      charts.get("topLoyalClients"), finalData, "topLoyalClients");


            document.add(new AreaBreak());

            Paragraph page2Title = new Paragraph("PAGE 2 - ANALYSE DES PRESTATIONS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(page2Title);

            addSection(document, "5. Répartition par Type de Prestation",
                      charts.get("serviceTypeDistribution"), finalData, "serviceTypeDistribution");

            addSection(document, "6. Fréquence d'Utilisation des Prestations",
                      charts.get("serviceFrequency"), finalData, "serviceFrequency");

            addSection(document, "7. Top 5 des Prestations les Plus Demandées",
                      charts.get("topPopularServices"), finalData, "topPopularServices");

            addSection(document, "8. Analyse des Livraisons par Statut",
                      charts.get("deliveryStatus"), finalData, "deliveryStatus");

            addConclusions(document, finalData);

            return outputPath.toString();
        }
    }

    private void addTitlePage(Document document, Map<String, Object> data) {
        Paragraph title = new Paragraph("RAPPORT D'ANALYSE ECODELI")
            .setFontSize(24)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30);
        document.add(title);

        Paragraph subtitle = new Paragraph("Analyse des Performances et Statistiques")
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(50);
        document.add(subtitle);

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(60))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        infoTable.addCell(createCell("Date de génération:", true));
        infoTable.addCell(createCell(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")), false));

        infoTable.addCell(createCell("Période d'analyse:", true));
        infoTable.addCell(createCell("Données cumulées", false));

        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        List<Map<String, Object>> services = (List<Map<String, Object>>) data.get("services");

        infoTable.addCell(createCell("Utilisateurs analysés:", true));
        infoTable.addCell(createCell(String.valueOf(users != null ? users.size() : 0), false));

        infoTable.addCell(createCell("Livraisons analysées:", true));
        infoTable.addCell(createCell(String.valueOf(livraisons != null ? livraisons.size() : 0), false));

        infoTable.addCell(createCell("Services analysés:", true));
        infoTable.addCell(createCell(String.valueOf(services != null ? services.size() : 0), false));

        document.add(infoTable);

        String generatedAt = (String) data.get("generatedAt");
        if (generatedAt != null) {
            Paragraph note = new Paragraph("\nNote: Ce rapport inclut des données générées automatiquement pour compléter l'analyse.")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
            document.add(note);
        }
    }


    private void addExecutiveSummary(Document document, Map<String, Object> data) {
        document.add(new AreaBreak());

        Paragraph title = new Paragraph("RÉSUMÉ EXÉCUTIF")
            .setFontSize(18)
            .setBold()
            .setMarginBottom(20);
        document.add(title);

        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        List<Map<String, Object>> services = (List<Map<String, Object>>) data.get("services");

        int totalUsers = users != null ? users.size() : 0;
        int totalLivraisons = livraisons != null ? livraisons.size() : 0;
        int totalServices = services != null ? services.size() : 0;

        double revenusLivraisons = livraisons != null ?
            livraisons.stream().mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0)).sum() : 0;
        double revenusServices = services != null ?
            services.stream().mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0)).sum() : 0;

        String summary = String.format(
            "La plateforme EcoDeli présente une activité %s avec %d utilisateurs actifs répartis " +
            "entre clients, commerçants et livreurs. Au total, %d livraisons ont été effectuées " +
            "générant un chiffre d'affaires de %.2f€, complétées par %d services additionnels " +
            "représentant %.2f€ de revenus supplémentaires.\n\n" +
            "L'analyse révèle un écosystème %s avec un potentiel de croissance significatif, " +
            "notamment dans l'optimisation des processus de livraison et l'expansion " +
            "des services proposés.",
            totalUsers > 50 ? "soutenue" : "en développement",
            totalUsers, totalLivraisons, revenusLivraisons, totalServices, revenusServices,
            (revenusLivraisons + revenusServices) > 5000 ? "robuste" : "émergent"
        );

        document.add(new Paragraph(summary).setTextAlignment(TextAlignment.JUSTIFIED));
    }


    private void addSection(Document document, String title, JFreeChart chart,
                           Map<String, Object> data, String chartType) throws IOException {

        document.add(new AreaBreak());

        Paragraph sectionTitle = new Paragraph(title)
            .setFontSize(16)
            .setBold()
            .setMarginBottom(15);
        document.add(sectionTitle);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 400);

        com.itextpdf.io.image.ImageData imageData =
            com.itextpdf.io.image.ImageDataFactory.create(chartStream.toByteArray());
        Image chartImage = new Image(imageData);
        chartImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        chartImage.setMarginBottom(15);

        document.add(chartImage);

        String analysis = generateAnalysis(chartType, data);
        document.add(new Paragraph(analysis).setTextAlignment(TextAlignment.JUSTIFIED));
    }


    private String generateAnalysis(String chartType, Map<String, Object> data) {
        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");
        List<Map<String, Object>> services = (List<Map<String, Object>>) data.get("services");
        List<Map<String, Object>> clients = (List<Map<String, Object>>) data.get("clients");

        switch (chartType) {
            case "userDistribution":
                long clientsCount = users != null ? users.stream().filter(u -> "CLIENT".equals(u.get("type"))).count() : 0;
                long commercants = users != null ? users.stream().filter(u -> "COMMERCANT".equals(u.get("type"))).count() : 0;
                long livreurs = users != null ? users.stream().filter(u -> "LIVREUR".equals(u.get("type"))).count() : 0;

                return String.format("La répartition des utilisateurs montre %d clients (%.1f%%), %d commerçants (%.1f%%) " +
                    "et %d livreurs (%.1f%%). Cette distribution %s un équilibre %s entre l'offre et la demande sur la plateforme.",
                    clientsCount, clientsCount * 100.0 / (clientsCount + commercants + livreurs),
                    commercants, commercants * 100.0 / (clientsCount + commercants + livreurs),
                    livreurs, livreurs * 100.0 / (clientsCount + commercants + livreurs),
                    Math.abs(clientsCount - commercants) < 10 ? "révèle" : "suggère",
                    Math.abs(clientsCount - commercants) < 10 ? "sain" : "à optimiser");

            case "clientRevenueDistribution":
                int tranche0_100 = 0, tranche101_500 = 0, tranche500Plus = 0;
                if (clients != null) {
                    for (Map<String, Object> client : clients) {
                        Double ca = (Double) client.getOrDefault("totalDepense", 0.0);
                        if (ca <= 100) tranche0_100++;
                        else if (ca <= 500) tranche101_500++;
                        else tranche500Plus++;
                    }
                }
                return String.format("L'analyse du chiffre d'affaires révèle %d clients avec un CA de 0-100€ (%.1f%%), " +
                    "%d clients entre 101-500€ (%.1f%%) et %d clients premium >500€ (%.1f%%). Cette segmentation permet " +
                    "d'adapter les stratégies commerciales selon la valeur client.",
                    tranche0_100, tranche0_100 * 100.0 / (clients != null ? clients.size() : 1),
                    tranche101_500, tranche101_500 * 100.0 / (clients != null ? clients.size() : 1),
                    tranche500Plus, tranche500Plus * 100.0 / (clients != null ? clients.size() : 1));

            case "topLoyalClients":
                return "Le classement des clients les plus fidèles met en évidence l'importance de la rétention client. " +
                    "Ces clients premium génèrent une part significative du chiffre d'affaires et méritent une attention " +
                    "particulière pour maintenir leur engagement et développer des programmes de fidélité ciblés.";

            case "serviceTypeDistribution":
                return "La répartition par type de prestation montre la prédominance des services de transport et livraison, " +
                    "suivis des services à domicile. Cette diversification des services permet à EcoDeli de répondre " +
                    "aux besoins variés de sa clientèle et de développer de nouveaux segments de marché.";

            case "serviceFrequency":
                return "L'analyse de fréquence révèle que les services de transport sont les plus demandés, confirmant " +
                    "le positionnement core business d'EcoDeli. Les services personnels et de formation représentent " +
                    "des opportunités de croissance à développer pour diversifier l'offre.";

            case "topPopularServices":
                return "Le top 5 des prestations populaires confirme la forte demande pour la livraison express et " +
                    "les services du quotidien. Cette hiérarchisation permet d'optimiser l'allocation des ressources " +
                    "et de prioriser le développement des services les plus rentables.";

            case "deliveryStatus":
                long terminees = livraisons != null ? livraisons.stream().filter(l -> "TERMINEE".equals(l.get("statut"))).count() : 0;
                long enCours = livraisons != null ? livraisons.stream().filter(l -> "EN_COURS".equals(l.get("statut"))).count() : 0;
                double tauxReussite = livraisons != null && !livraisons.isEmpty() ?
                    (terminees * 100.0 / livraisons.size()) : 0;

                return String.format("Le taux de réussite des livraisons s'élève à %.1f%% avec %d livraisons terminées " +
                    "sur un total de %d. %d livraisons sont actuellement en cours, indiquant une activité %s de la plateforme.",
                    tauxReussite, terminees, livraisons != null ? livraisons.size() : 0, enCours,
                    enCours > 5 ? "soutenue" : "modérée");

            case "revenueEvolution":
                double revenusTotal = 0;
                if (livraisons != null) revenusTotal += livraisons.stream().mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0)).sum();
                if (services != null) revenusTotal += services.stream().mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0)).sum();

                return String.format("L'évolution des revenus montre une tendance %s avec un chiffre d'affaires total " +
                    "de %.2f€. La diversification entre livraisons et services additionnels offre une base solide " +
                    "pour la croissance future de l'entreprise.",
                    revenusTotal > 1000 ? "positive" : "en développement", revenusTotal);

            default:
                return "L'analyse de ce graphique révèle des tendances importantes pour l'optimisation " +
                    "des performances de la plateforme EcoDeli et le développement de stratégies commerciales adaptées.";
        }
    }


    private void addConclusions(Document document, Map<String, Object> data) {
        document.add(new AreaBreak());

        Paragraph title = new Paragraph("CONCLUSIONS ET RECOMMANDATIONS")
            .setFontSize(18)
            .setBold()
            .setMarginBottom(20);
        document.add(title);

        String conclusions = "L'analyse des données EcoDeli révèle une plateforme en croissance avec des opportunités " +
            "d'amélioration significatives. Les recommandations suivantes sont proposées :\n\n" +
            "• Optimiser l'équilibre entre le nombre de clients et de commerçants\n" +
            "• Améliorer les processus de livraison pour réduire les délais\n" +
            "• Développer des services additionnels pour diversifier les revenus\n" +
            "• Mettre en place des indicateurs de performance en temps réel\n\n" +
            "La plateforme dispose d'un potentiel solide pour devenir un acteur majeur " +
            "de la livraison écologique et des services de proximité.";

        document.add(new Paragraph(conclusions).setTextAlignment(TextAlignment.JUSTIFIED));

        Paragraph footer = new Paragraph("\n\nRapport généré automatiquement par EcoDeli Analytics")
            .setFontSize(10)
            .setItalic()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30);
        document.add(footer);
    }

    private String getChartTitle(String chartKey) {
        switch (chartKey) {
            case "userDistribution":
                return "1. Répartition des Utilisateurs par Type";
            case "clientRevenueDistribution":
                return "2. Répartition des Clients par Chiffre d'Affaires";
            case "revenueEvolution":
                return "3. Évolution des Revenus Mensuels";
            case "topLoyalClients":
                return "4. Top 5 des Clients les Plus Fidèles";
            case "serviceTypeDistribution":
                return "5. Répartition par Type de Prestation";
            case "serviceFrequency":
                return "6. Fréquence d'Utilisation des Prestations";
            case "topPopularServices":
                return "7. Top 5 des Prestations les Plus Demandées";
            case "deliveryStatus":
                return "8. Analyse des Livraisons par Statut";
            case "performance":
                return "9. Performances Globales";
            default:
                return "Analyse " + chartKey;
        }
    }


    private Cell createCell(String content, boolean bold) {
        Cell cell = new Cell().add(new Paragraph(content));
        if (bold) {
            cell.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }
        return cell;
    }


    private boolean needDataGeneration(Map<String, Object> data) {
        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
        List<Map<String, Object>> livraisons = (List<Map<String, Object>>) data.get("livraisons");

        int totalUsers = users != null ? users.size() : 0;
        int totalLivraisons = livraisons != null ? livraisons.size() : 0;

        return totalUsers < 30 || totalLivraisons < 30;
    }


    private Map<String, Object> mergeData(Map<String, Object> realData, Map<String, Object> generatedData) {
        Map<String, Object> merged = new java.util.HashMap<>(generatedData);

        if (realData.get("users") != null) {
            List<Map<String, Object>> realUsers = (List<Map<String, Object>>) realData.get("users");
            List<Map<String, Object>> genUsers = (List<Map<String, Object>>) generatedData.get("users");
            realUsers.addAll(genUsers);
            merged.put("users", realUsers);
        }
        return merged;
    }
}
