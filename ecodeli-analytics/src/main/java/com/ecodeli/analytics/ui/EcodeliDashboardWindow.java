package com.ecodeli.analytics.ui;

import com.ecodeli.analytics.services.BackendApiService;
import com.ecodeli.analytics.services.ChartGeneratorService;
import com.ecodeli.analytics.services.DataGeneratorService;
import com.ecodeli.analytics.services.PdfReportService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.Dimension;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EcodeliDashboardWindow {

    private final BackendApiService backendApiService;
    private final ChartGeneratorService chartGeneratorService;
    private final DataGeneratorService dataGeneratorService;
    private final PdfReportService pdfReportService;

    private Stage primaryStage;
    private ComboBox<String> periodFilter;
    private ComboBox<String> categoryFilter;
    private Button exportPdfButton;
    private Label statusLabel;
    private GridPane kpiContainer;
    private GridPane clientsChartsContainer;
    private GridPane prestationsChartsContainer;

    private Map<String, Object> currentData;
    private Map<String, JFreeChart> currentCharts;

    public void show(Stage stage) {
        this.primaryStage = stage;

        stage.setTitle("EcoDeli Analytics Dashboard");
        stage.setWidth(1600);
        stage.setHeight(1000);
        stage.setMinWidth(1400);
        stage.setMinHeight(900);

        BorderPane mainLayout = createMainLayout();

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.show();

        loadInitialData();
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createMainBackgroundStyle());

        VBox header = createEcodeliHeader();
        root.setTop(header);

        ScrollPane centerScrollPane = new ScrollPane();
        centerScrollPane.setFitToWidth(true);
        centerScrollPane.setStyle("-fx-background-color: transparent;");

        VBox centerContent = createCenterContent();
        centerScrollPane.setContent(centerContent);
        root.setCenter(centerScrollPane);

        HBox footer = createEcodeliFooter();
        root.setBottom(footer);

        return root;
    }


    private VBox createEcodeliHeader() {
        VBox header = new VBox();
        header.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createHeaderStyle());

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(15);

        Label emojiLabel = new Label("🌱");
        emojiLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.NORMAL, 32));

        VBox titleBox = new VBox();
        titleBox.setSpacing(5);

        Label titleLabel = new Label("EcoDeli Analytics");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY));

        Label subtitleLabel = new Label("Dashboard des Performances");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUBTLE));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        titleRow.getChildren().addAll(emojiLabel, titleBox);

        HBox filtersRow = new HBox();
        filtersRow.setAlignment(Pos.CENTER_LEFT);
        filtersRow.setSpacing(25);

        Label periodLabel = new Label("📅 Période:");
        periodLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        periodLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY));

        periodFilter = new ComboBox<>();
        periodFilter.getItems().addAll("Mois actuel", "Trimestre", "Semestre", "Année");
        periodFilter.setValue("Mois actuel");
        periodFilter.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createComboBoxStyle());
        periodFilter.setOnAction(e -> applyFilters());

        statusLabel = new Label("Prêt");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUCCESS));

        filtersRow.getChildren().addAll(
            periodLabel, periodFilter,
            new Region(),
            statusLabel
        );
        HBox.setHgrow(filtersRow.getChildren().get(filtersRow.getChildren().size() - 2), Priority.ALWAYS);

        header.getChildren().addAll(titleRow, filtersRow);
        return header;
    }


    private VBox createCenterContent() {
        VBox content = new VBox();
        content.setPadding(new Insets(30));
        content.setSpacing(30);

        VBox kpiSection = createKpiSection();

        VBox clientsSection = createClientsSection();

        VBox prestationsSection = createPrestationsSection();

        content.getChildren().addAll(kpiSection, clientsSection, prestationsSection);
        return content;
    }

    private VBox createKpiSection() {
        VBox section = new VBox();
        section.setSpacing(20);

        Label sectionTitle = new Label("📊 Indicateurs Clés de Performance");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        sectionTitle.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));

        kpiContainer = new GridPane();
        kpiContainer.setHgap(25);
        kpiContainer.setVgap(25);
        kpiContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            kpiContainer.getColumnConstraints().add(col);
        }

        VBox kpi1 = createKpiCard("👥", "Total Utilisateurs", "91", com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY);
        VBox kpi2 = createKpiCard("💰", "CA Mensuel", "6.2K€", com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY);
        VBox kpi3 = createKpiCard("⭐", "Clients Actifs", "85%", com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY);
        VBox kpi4 = createKpiCard("📈", "Taux Succès", "92%", com.ecodeli.analytics.ui.styles.ColorPalette.WHITE_PRIMARY);

        kpiContainer.add(kpi1, 0, 0);
        kpiContainer.add(kpi2, 1, 0);
        kpiContainer.add(kpi3, 2, 0);
        kpiContainer.add(kpi4, 3, 0);

        section.getChildren().addAll(sectionTitle, kpiContainer);
        return section;
    }

    private VBox createKpiCard(String icon, String label, String value, String bgColor) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(12);
        card.setPadding(new Insets(30));
        card.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createKpiCardStyle());
        card.setMinHeight(140);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.NORMAL, 28));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));

        Label labelLabel = new Label(label);
        labelLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        labelLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.TEXT_MEDIUM));

        card.getChildren().addAll(iconLabel, valueLabel, labelLabel);
        return card;
    }

    private VBox createClientsSection() {
        VBox section = new VBox();
        section.setSpacing(20);

        Label sectionTitle = new Label("👥 Analyse Clients & Commerçants");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        sectionTitle.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));
        clientsChartsContainer = new GridPane();
        clientsChartsContainer.setHgap(25);
        clientsChartsContainer.setVgap(25);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            clientsChartsContainer.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(50);
            clientsChartsContainer.getRowConstraints().add(row);
        }

        VBox placeholder1 = createChartPlaceholder("📊 Répartition par Type", "Clients, Commerçants, Livreurs");
        VBox placeholder2 = createChartPlaceholder("💰 Répartition par CA", "Tranches de chiffre d'affaires");
        VBox placeholder3 = createChartPlaceholder("📈 Évolution Revenus", "Tendances mensuelles");
        VBox placeholder4 = createChartPlaceholder("⭐ Top 5 Clients Fidèles", "Clients les plus actifs");

        clientsChartsContainer.add(placeholder1, 0, 0);
        clientsChartsContainer.add(placeholder2, 1, 0);
        clientsChartsContainer.add(placeholder3, 0, 1);
        clientsChartsContainer.add(placeholder4, 1, 1);

        section.getChildren().addAll(sectionTitle, clientsChartsContainer);
        return section;
    }

    private VBox createPrestationsSection() {
        VBox section = new VBox();
        section.setSpacing(20);

        Label sectionTitle = new Label("🛠️ Analyse des Prestations");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        sectionTitle.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));

        prestationsChartsContainer = new GridPane();
        prestationsChartsContainer.setHgap(25);
        prestationsChartsContainer.setVgap(25);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            prestationsChartsContainer.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(50);
            prestationsChartsContainer.getRowConstraints().add(row);
        }

        VBox placeholder1 = createChartPlaceholder("🛠️ Types de Prestations", "6 catégories de services");
        VBox placeholder2 = createChartPlaceholder("📈 Fréquence d'Utilisation", "Utilisation par mois");
        VBox placeholder3 = createChartPlaceholder("📦 Statuts Livraisons", "En cours, terminées, annulées");
        VBox placeholder4 = createChartPlaceholder("🏆 Top 5 Populaires", "Prestations les plus demandées");

        prestationsChartsContainer.add(placeholder1, 0, 0);
        prestationsChartsContainer.add(placeholder2, 1, 0);
        prestationsChartsContainer.add(placeholder3, 0, 1);
        prestationsChartsContainer.add(placeholder4, 1, 1);

        section.getChildren().addAll(sectionTitle, prestationsChartsContainer);
        return section;
    }

    private VBox createChartPlaceholder(String title, String description) {
        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(15);
        placeholder.setPadding(new Insets(35));
        placeholder.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createChartContainerStyle());
        placeholder.setMinHeight(380);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.TEXT_MEDIUM));

        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(50, 50);
        loading.setStyle("-fx-accent: " + com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUCCESS + ";");

        placeholder.getChildren().addAll(titleLabel, descLabel, loading);
        return placeholder;
    }

    private HBox createEcodeliFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(25));
        footer.setSpacing(20);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createFooterStyle());

        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createSecondaryButtonStyle());
        refreshButton.setOnAction(e -> loadInitialData());

        exportPdfButton = new Button("📄 Exporter PDF");
        exportPdfButton.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createPrimaryButtonStyle());
        exportPdfButton.setOnAction(e -> exportPdf());
        exportPdfButton.setDisable(true);

        footer.getChildren().addAll(refreshButton, exportPdfButton);
        return footer;
    }


    private void loadInitialData() {
        Platform.runLater(() -> {
            statusLabel.setText("Chargement...");
            statusLabel.setTextFill(Color.web("#f59e0b"));
            exportPdfButton.setDisable(true);
        });

        Task<Map<String, Object>> loadTask = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return dataGeneratorService.generateCompleteDataset();
            }
        };

        loadTask.setOnSucceeded(e -> {
            currentData = loadTask.getValue();
            generateCharts();
        });

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Erreur");
                statusLabel.setTextFill(Color.web("#ef4444"));
            });
        });
        new Thread(loadTask).start();
    }

    private void generateCharts() {
        Task<Map<String, JFreeChart>> chartTask = new Task<Map<String, JFreeChart>>() {
            @Override
            protected Map<String, JFreeChart> call() throws Exception {
                return chartGeneratorService.generateAllCharts(currentData);
            }
        };
        chartTask.setOnSucceeded(e -> {
            currentCharts = chartTask.getValue();
            displayCharts();
        });
        new Thread(chartTask).start();
    }

    private void displayCharts() {
        Platform.runLater(() -> {
            displayClientCharts();
            displayPrestationCharts();
            updateKpiValues();

            statusLabel.setText("Dashboard prêt");
            statusLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUCCESS));
            exportPdfButton.setDisable(false);
        });
    }


    private void displayClientCharts() {
        if (currentCharts.containsKey("userDistribution")) {
            SwingNode node1 = createInteractiveChartNode(currentCharts.get("userDistribution"), "userDistribution");
            clientsChartsContainer.add(node1, 0, 0);
        }

        if (currentCharts.containsKey("clientRevenueDistribution")) {
            SwingNode node2 = createInteractiveChartNode(currentCharts.get("clientRevenueDistribution"), "clientRevenueDistribution");
            clientsChartsContainer.add(node2, 1, 0);
        }

        if (currentCharts.containsKey("revenueEvolution")) {
            SwingNode node3 = createInteractiveChartNode(currentCharts.get("revenueEvolution"), "revenueEvolution");
            clientsChartsContainer.add(node3, 0, 1);
        }

        if (currentCharts.containsKey("topLoyalClients")) {
            SwingNode node4 = createInteractiveChartNode(currentCharts.get("topLoyalClients"), "topLoyalClients");
            clientsChartsContainer.add(node4, 1, 1);
        }
    }


    private void displayPrestationCharts() {
        if (currentCharts.containsKey("serviceTypeDistribution")) {
            SwingNode node1 = createInteractiveChartNode(currentCharts.get("serviceTypeDistribution"), "serviceTypeDistribution");
            prestationsChartsContainer.add(node1, 0, 0);
        }

        if (currentCharts.containsKey("serviceFrequency")) {
            SwingNode node2 = createInteractiveChartNode(currentCharts.get("serviceFrequency"), "serviceFrequency");
            prestationsChartsContainer.add(node2, 1, 0);
        }

        if (currentCharts.containsKey("deliveryStatus")) {
            SwingNode node3 = createInteractiveChartNode(currentCharts.get("deliveryStatus"), "deliveryStatus");
            prestationsChartsContainer.add(node3, 0, 1);
        }

        if (currentCharts.containsKey("topPopularServices")) {
            SwingNode node4 = createInteractiveChartNode(currentCharts.get("topPopularServices"), "topPopularServices");
            prestationsChartsContainer.add(node4, 1, 1);
        }
    }


    private SwingNode createInteractiveChartNode(JFreeChart chart, String chartType) {
        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(750, 380));
            chartPanel.setMouseWheelEnabled(true);

            chartPanel.addChartMouseListener(new ChartMouseListener() {
                @Override
                public void chartMouseClicked(ChartMouseEvent event) {
                    handleChartClick(event, chartType);
                }

                @Override
                public void chartMouseMoved(ChartMouseEvent event) {
                }
            });

            swingNode.setContent(chartPanel);
        });

        return swingNode;
    }

    private void handleChartClick(ChartMouseEvent event, String chartType) {
        if (event.getEntity() instanceof PieSectionEntity) {
            PieSectionEntity section = (PieSectionEntity) event.getEntity();
            String label = section.getSectionKey().toString();
            Number value = section.getDataset().getValue(section.getSectionKey());

            Platform.runLater(() -> showValueDetails(label, value, chartType));

        } else if (event.getEntity() instanceof CategoryItemEntity) {
            CategoryItemEntity item = (CategoryItemEntity) event.getEntity();
            String label = item.getRowKey().toString();
            Number value = item.getDataset().getValue(item.getRowKey(), item.getColumnKey());

            Platform.runLater(() -> showValueDetails(label, value, chartType));
        }
    }


    private void showValueDetails(String label, Number value, String chartType) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(primaryStage);
        modal.setTitle("📊 EcoDeli - Détails " + label);
        modal.setResizable(false);

        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setSpacing(25);
        content.setPadding(new Insets(40));
        content.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createModalStyle());

        Label titleLabel = new Label("📊 " + label);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_PRIMARY));

        Label valueLabel = new Label(value.toString());
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        valueLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUCCESS));

        String unit = getUnitForChart(chartType);
        Label unitLabel = new Label(unit);
        unitLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        unitLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.TEXT_MEDIUM));

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(com.ecodeli.analytics.ui.styles.StyleFactory.createSecondaryButtonStyle());
        closeButton.setOnAction(e -> modal.close());

        content.getChildren().addAll(titleLabel, valueLabel, unitLabel, closeButton);
        Scene modalScene = new Scene(content, 350, 300);
        modal.setScene(modalScene);
        modal.show();
    }

    private String getUnitForChart(String chartType) {
        switch (chartType) {
            case "userDistribution":
                return "utilisateurs";
            case "clientRevenueDistribution":
                return "clients";
            case "serviceTypeDistribution":
                return "services";
            case "serviceFrequency":
                return "utilisations/mois";
            case "deliveryStatus":
                return "livraisons";
            case "topLoyalClients":
                return "commandes";
            case "topPopularServices":
                return "demandes";
            default:
                return "éléments";
        }
    }


    private void updateKpiValues() {
        if (currentData == null) return;

        java.util.List<Map<String, Object>> users = (java.util.List<Map<String, Object>>) currentData.get("users");
        java.util.List<Map<String, Object>> livraisons = (java.util.List<Map<String, Object>>) currentData.get("livraisons");
        java.util.List<Map<String, Object>> services = (java.util.List<Map<String, Object>>) currentData.get("services");

        int totalUsers = users != null ? users.size() : 0;
        double totalRevenue = 0;
        if (livraisons != null) {
            totalRevenue += livraisons.stream().mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0)).sum();
        }
        if (services != null) {
            totalRevenue += services.stream().mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0)).sum();
        }

        long clientsActifs = users != null ? users.stream().filter(u -> "CLIENT".equals(u.get("type"))).count() : 0;
        long livraisonsTerminees = livraisons != null ? livraisons.stream().filter(l -> "TERMINEE".equals(l.get("statut"))).count() : 0;
        double tauxSucces = livraisons != null && !livraisons.isEmpty() ?
            (livraisonsTerminees * 100.0 / livraisons.size()) : 0;
    }

    private void applyFilters() {
        String period = periodFilter.getValue();

        Platform.runLater(() -> {
            statusLabel.setText("Filtrage...");
            statusLabel.setTextFill(Color.web("#f59e0b"));
        });

        loadInitialData();
    }

    private void exportPdf() {
        Platform.runLater(() -> {
            statusLabel.setText("Génération PDF...");
            statusLabel.setTextFill(Color.web("#f59e0b"));
            exportPdfButton.setDisable(true);
        });

        Task<String> exportTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                if (currentData == null || currentCharts == null) {
                    throw new Exception("Aucune donnée ou graphique disponible");
                }
                return pdfReportService.generatePdfReport(currentData, currentCharts);
            }
        };

        exportTask.setOnSucceeded(e -> {
            String pdfPath = exportTask.getValue();
            Platform.runLater(() -> {
                statusLabel.setText("PDF exporté: " + pdfPath);
                statusLabel.setTextFill(Color.web(com.ecodeli.analytics.ui.styles.ColorPalette.GREEN_SUCCESS));
                exportPdfButton.setDisable(false);
            });
        });

        exportTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Erreur PDF");
                statusLabel.setTextFill(Color.web("#ef4444"));
                exportPdfButton.setDisable(false);
            });
        });

        new Thread(exportTask).start();
    }
}
