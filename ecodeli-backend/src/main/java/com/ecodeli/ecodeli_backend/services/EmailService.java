package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(Utilisateur utilisateur) {
        try {
            logger.info("Envoi de l'email de bienvenue pour : {}", utilisateur.getEmail());

            Map<String, String> templateData = new HashMap<>();
            templateData.put("prenom", utilisateur.getPrenom());
            templateData.put("nom", utilisateur.getNom());
            templateData.put("email", utilisateur.getEmail());
            templateData.put("role", getRoleDisplayName(utilisateur.getType()));
            templateData.put("dateInscription", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));

            String htmlContent = loadWelcomeTemplate(templateData);

            sendHtmlEmail(
                utilisateur.getEmail(),
                "🌱 Bienvenue sur EcoDeli ! Votre inscription est confirmée",
                htmlContent
            );

            logger.info("Email de bienvenue envoyé avec succès à : {}", utilisateur.getEmail());

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue pour : {}", utilisateur.getEmail(), e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail, "EcoDeli");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String loadWelcomeTemplate(Map<String, String> data) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/welcome-email.html");
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        String roleFeatures = getRoleFeaturesHtml(data.get("role"));
        template = template.replace("<!-- Contenu dynamique selon le rôle -->", roleFeatures);

        return template;
    }

    private String getRoleFeaturesHtml(String role) {
        Map<String, String[]> roleFeatures = new HashMap<>();

        roleFeatures.put("CLIENT", new String[]{
            "📦 Créer des demandes de livraison",
            "👀 Voir les candidatures des livreurs",
            "📊 Suivre vos commandes en temps réel",
            "⭐ Évaluer les services reçus"
        });

        roleFeatures.put("LIVREUR", new String[]{
            "🚚 Consulter les annonces disponibles",
            "✋ Postuler pour des livraisons",
            "📍 Gérer vos trajets et livraisons",
            "💰 Suivre vos revenus"
        });

        roleFeatures.put("PRESTATAIRE", new String[]{
            "🔧 Proposer vos services",
            "📋 Gérer vos prestations",
            "👥 Répondre aux demandes clients",
            "📈 Développer votre activité"
        });

        roleFeatures.put("COMMERCANT", new String[]{
            "🏪 Gérer votre boutique",
            "📦 Organiser vos livraisons",
            "🤝 Collaborer avec des livreurs",
            "📊 Suivre vos ventes"
        });

        String[] features = roleFeatures.getOrDefault(role, new String[]{"🌱 Découvrir EcoDeli"});

        StringBuilder html = new StringBuilder();
        for (String feature : features) {
            html.append("<li>").append(feature).append("</li>");
        }

        return html.toString();
    }

    private String getRoleDisplayName(String role) {
        Map<String, String> roleDisplayNames = new HashMap<>();
        roleDisplayNames.put("CLIENT", "Client");
        roleDisplayNames.put("LIVREUR", "Livreur");
        roleDisplayNames.put("PRESTATAIRE", "Prestataire");
        roleDisplayNames.put("COMMERCANT", "Commerçant");
        roleDisplayNames.put("ADMIN", "Administrateur");

        return roleDisplayNames.getOrDefault(role, role);
    }

    @Async
    public void sendCandidatureNotification(String clientEmail, String livreurNom, String annonceTitle) {
        try {
            String subject = "Nouvelle candidature reçue pour : " + annonceTitle;
            String content = String.format(
                "<h2>Nouvelle candidature !</h2>" +
                "<p>Le livreur <strong>%s</strong> s'est proposé pour votre annonce <strong>%s</strong>.</p>" +
                "<p><a href=\"http://localhost:5173/client\">Voir les candidatures</a></p>",
                livreurNom, annonceTitle
            );

            sendHtmlEmail(clientEmail, subject, content);
            logger.info("Email de notification candidature envoyé à : {}", clientEmail);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification candidature", e);
        }
    }

    @Async
    public void sendCandidaturePartielleNotification(String clientEmail, String livreurNom, String annonceTitle, String segment, String entrepot) {
        try {
            String subject = "🚚 Nouvelle candidature partielle - " + annonceTitle;
            String entrepotInfo = (entrepot != null) ? " via l'entrepôt " + entrepot : "";

            String content = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;\">" +
                "<div style=\"background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"margin: 0; font-size: 24px;\">🌱 EcoDeli</h1>" +
                "<p style=\"margin: 5px 0 0 0; opacity: 0.9;\">Candidature partielle reçue</p>" +
                "</div>" +
                "<div style=\"background: white; padding: 30px; border-radius: 0 0 10px 10px;\">" +
                "<h2 style=\"color: #333; margin-top: 0;\">Nouvelle candidature pour le %s</h2>" +
                "<p style=\"color: #666;\">Annonce : <strong>%s</strong></p>" +
                "<p style=\"color: #666;\">Livreur : <strong>%s</strong></p>" +
                "<p style=\"color: #666;\">Segment : <strong>%s</strong>%s</p>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/client\" style=\"background: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;\">Voir les candidatures</a>" +
                "</div>" +
                "</div>" +
                "</div>",
                segment, annonceTitle, livreurNom, segment, entrepotInfo
            );

            sendHtmlEmail(clientEmail, subject, content);
            logger.info("Email de notification candidature partielle envoyé à : {}", clientEmail);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification candidature partielle", e);
        }
    }

    @Async
    public void sendSegment2Notification(String livreurEmail, String annonceTitle, String entrepotVille) {
        try {
            String subject = "🚚 Segment 1 terminé - Vous pouvez démarrer !";

            String content = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;\">" +
                "<div style=\"background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"margin: 0; font-size: 24px;\">🌱 EcoDeli</h1>" +
                "<p style=\"margin: 5px 0 0 0; opacity: 0.9;\">Segment 2 prêt</p>" +
                "</div>" +
                "<div style=\"background: white; padding: 30px; border-radius: 0 0 10px 10px;\">" +
                "<h2 style=\"color: #333; margin-top: 0;\">Votre segment peut commencer !</h2>" +
                "<p style=\"color: #666;\">Le segment 1 de la livraison <strong>%s</strong> vient d'être terminé.</p>" +
                "<p style=\"color: #666;\">Le colis vous attend à l'entrepôt <strong>%s</strong>.</p>" +
                "<div style=\"background: #e8f5e8; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;\">" +
                "<p style=\"margin: 0; color: #2e7d32;\"><strong>Action requise :</strong> Connectez-vous à votre espace livreur pour démarrer le segment 2.</p>" +
                "</div>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/livreur\" style=\"background: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;\">Démarrer le segment 2</a>" +
                "</div>" +
                "</div>" +
                "</div>",
                annonceTitle, entrepotVille
            );

            sendHtmlEmail(livreurEmail, subject, content);
            logger.info("Email segment 2 ready envoyé à : {}", livreurEmail);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification segment 2", e);
        }
    }

    @Async
    public void sendDeliveryCode(String destinataireEmail, String code, String annonceTitle) {
        try {
            String subject = "Code de validation livraison - " + annonceTitle;
            String htmlContent = String.format(
                """
                <h2>Code de validation de livraison</h2>
                <p>Bonjour,</p>
                <p>Votre livreur est arrivé pour la livraison de "<strong>%s</strong>".</p>
                <p>Voici votre code de validation :</p>
                <div style="font-size: 24px; font-weight: bold; color: #007bff; text-align: center; margin: 20px 0; padding: 15px; border: 2px solid #007bff; border-radius: 8px;">
                    %s
                </div>
                <p>Communiquez ce code au livreur pour confirmer la réception du colis.</p>
                <p><strong>Attention :</strong> Ce code expire dans 15 minutes.</p>
                <p>Cordialement,<br>L'équipe EcoDeli</p>
                """,
                annonceTitle, code
            );

            sendHtmlEmail(destinataireEmail, subject, htmlContent);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email code de validation : " + e.getMessage());
        }
    }

    @Async
    public void sendDeliveryOTP(String destinataireEmail, String otp, String annonceTitle) {
        try {
            String subject = "Code OTP livraison - " + annonceTitle;
            String htmlContent = String.format(
                """
                <h2>Code OTP de livraison</h2>
                <p>Bonjour,</p>
                <p>Votre livreur est arrivé pour la livraison de "<strong>%s</strong>".</p>
                <p>Voici votre code OTP :</p>
                <div style="font-size: 24px; font-weight: bold; color: #007bff; text-align: center; margin: 20px 0; padding: 15px; border: 2px solid #007bff; border-radius: 8px;">
                    %s
                </div>
                <p>Communiquez ce code au livreur pour confirmer la réception du colis.</p>
                <p><strong>Attention :</strong> Ce code expire dans 5 minutes.</p>
                <p>Cordialement,<br>L'équipe EcoDeli</p>
                """,
                annonceTitle, otp
            );

            sendHtmlEmail(destinataireEmail, subject, htmlContent);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email OTP de livraison : " + e.getMessage());
        }
    }
}
