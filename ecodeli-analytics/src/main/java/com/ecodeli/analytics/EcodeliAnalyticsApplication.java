package com.ecodeli.analytics;

import com.ecodeli.analytics.ui.EcodeliDashboardWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.class
})
public class EcodeliAnalyticsApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static String[] savedArgs;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("prism.order", "sw");

        savedArgs = args;

        Application.launch(EcodeliAnalyticsApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(EcodeliAnalyticsApplication.class, savedArgs);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            EcodeliDashboardWindow dashboard = springContext.getBean(EcodeliDashboardWindow.class);
            dashboard.show(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
