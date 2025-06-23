package com.ecodeli.ecodeli_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.models.Livraison;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(Utilisateur utilisateur) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(utilisateur.getEmail());
            helper.setSubject("Bienvenue chez EcoDeli ! 🌱");

            String htmlContent = buildWelcomeEmailContent(utilisateur);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de bienvenue envoyé avec succès à : {}", utilisateur.getEmail());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue à {} : {}",
                        utilisateur.getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email de bienvenue à {} : {}",
                        utilisateur.getEmail(), e.getMessage());
        }
    }

    public void sendDeliveryCodeToClient(Livraison livraison, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(livraison.getDestinataire().getEmail());
            helper.setSubject("Code de validation EcoDeli - Livraison #" + livraison.getIdLivraison());

            String htmlContent = buildDeliveryCodeClientEmailContent(livraison, code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Code de validation envoyé au client {} pour la livraison #{}",
                       livraison.getDestinataire().getEmail(), livraison.getIdLivraison());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi du code de validation au client {} : {}",
                        livraison.getDestinataire().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi du code de validation au client {} : {}",
                        livraison.getDestinataire().getEmail(), e.getMessage());
        }
    }

    public void sendDeliveryCodeToDriver(Livraison livraison, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(livraison.getAnnonce().getLivreur().getEmail());
            helper.setSubject("Code de validation EcoDeli - Dépôt entrepôt #" + livraison.getIdLivraison());

            String htmlContent = buildDeliveryCodeDriverEmailContent(livraison, code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Code de validation envoyé au livreur {} pour la livraison #{}",
                       livraison.getAnnonce().getLivreur().getEmail(), livraison.getIdLivraison());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi du code de validation au livreur {} : {}",
                        livraison.getAnnonce().getLivreur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi du code de validation au livreur {} : {}",
                        livraison.getAnnonce().getLivreur().getEmail(), e.getMessage());
        }
    }

    public void sendSegment2NotificationToDriver(Livraison livraison) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(livraison.getLivreurSegment2().getEmail());
            helper.setSubject("EcoDeli - Colis prêt pour collecte - Livraison #" + livraison.getIdLivraison());

            String htmlContent = buildSegment2NotificationEmailContent(livraison);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Notification segment 2 envoyée au livreur {} pour la livraison #{}",
                       livraison.getLivreurSegment2().getEmail(), livraison.getIdLivraison());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification segment 2 au livreur {} : {}",
                        livraison.getLivreurSegment2().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de la notification segment 2 au livreur {} : {}",
                        livraison.getLivreurSegment2().getEmail(), e.getMessage());
        }
    }

    private String buildWelcomeEmailContent(Utilisateur utilisateur) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bienvenue chez EcoDeli</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .container {
                        background-color: #ffffff;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #4CAF50;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #4CAF50;
                        margin-bottom: 10px;
                    }
                    .welcome-title {
                        color: #2E7D32;
                        font-size: 24px;
                        margin-bottom: 20px;
                    }
                    .content {
                        font-size: 16px;
                        margin-bottom: 20px;
                    }
                    .highlight {
                        background-color: #E8F5E8;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #4CAF50;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        font-size: 14px;
                        color: #666;
                        text-align: center;
                    }
                    .contact-info {
                        background-color: #f5f5f5;
                        padding: 15px;
                        border-radius: 5px;
                        margin-top: 20px;
                    }
                    .emoji {
                        font-size: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Votre plateforme éco-responsable</p>
                    </div>

                    <h1 class="welcome-title">Bonjour %s ! <span class="emoji">👋</span></h1>

                    <div class="content">
                        <p>Nous sommes ravis de vous accueillir dans la communauté <strong>EcoDeli</strong> !</p>

                        <div class="highlight">
                            <p><strong>🎉 Votre compte a été créé avec succès !</strong></p>
                            <p>Vous pouvez maintenant profiter de tous nos services éco-responsables pour une livraison plus verte et durable.</p>
                        </div>

                        <p>Avec EcoDeli, vous contribuez à :</p>
                        <ul>
                            <li><span class="emoji">🌍</span> Réduire l'impact environnemental des livraisons</li>
                            <li><span class="emoji">🚴</span> Promouvoir des modes de transport écologiques</li>
                            <li><span class="emoji">📦</span> Optimiser les circuits de livraison</li>
                            <li><span class="emoji">🤝</span> Soutenir l'économie locale</li>
                        </ul>

                        <p>N'hésitez pas à explorer notre plateforme et à découvrir toutes les fonctionnalités disponibles.</p>
                    </div>

                    <div class="contact-info">
                        <h3 style="color: #2E7D32; margin-top: 0;">📞 Besoin d'aide ?</h3>
                        <p>Notre équipe est là pour vous accompagner :</p>
                        <p>
                            <strong>Email :</strong> ecodeli.nepasrepondre@gmail.com<br>
                            <strong>Plateforme :</strong> <a href="#" style="color: #4CAF50;">ecodeli.com</a>
                        </p>
                    </div>

                    <div class="footer">
                        <p>Merci de faire confiance à EcoDeli pour vos livraisons éco-responsables !</p>
                        <p style="margin-top: 15px;">
                            <strong>L'équipe EcoDeli</strong> 🌱
                        </p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">
                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.<br>
                            © 2025 EcoDeli - Tous droits réservés
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(utilisateur.getPrenom());
    }

    private String buildDeliveryCodeClientEmailContent(Livraison livraison, String code) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Code de validation EcoDeli</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .container {
                        background-color: #ffffff;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #FF9800;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #4CAF50;
                        margin-bottom: 10px;
                    }
                    .code-section {
                        background: linear-gradient(135deg, #FF9800, #F57C00);
                        color: white;
                        padding: 25px;
                        border-radius: 10px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .code {
                        font-size: 36px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        margin: 15px 0;
                        background-color: rgba(255,255,255,0.2);
                        padding: 15px;
                        border-radius: 8px;
                    }
                    .content {
                        font-size: 16px;
                        margin-bottom: 20px;
                    }
                    .delivery-info {
                        background-color: #FFF8E1;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #FF9800;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        font-size: 14px;
                        color: #666;
                        text-align: center;
                    }
                    .emoji {
                        font-size: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Livraison éco-responsable</p>
                    </div>

                    <h1 style="color: #E65100; font-size: 24px; margin-bottom: 20px;">
                        Votre livreur est arrivé ! <span class="emoji">🚚</span>
                    </h1>

                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>

                        <p>Votre livreur EcoDeli est arrivé à votre adresse. Pour confirmer la réception de votre colis, 
                        veuillez communiquer le code de validation suivant à votre livreur :</p>
                    </div>

                    <div class="code-section">
                        <p style="margin: 0; font-size: 18px;">🔑 CODE DE VALIDATION</p>
                        <div class="code">%s</div>
                        <p style="margin: 0; font-size: 14px; opacity: 0.9;">Ce code est valide pendant 15 minutes</p>
                    </div>

                    <div class="delivery-info">
                        <h3 style="color: #E65100; margin-top: 0;">📦 Détails de votre livraison</h3>
                        <p><strong>Livraison :</strong> #%d</p>
                        <p><strong>Expéditeur :</strong> %s %s</p>
                        <p><strong>Adresse de livraison :</strong> %s, %s</p>
                        %s
                    </div>

                    <div class="content">
                        <p><strong>⚠️ Important :</strong> Ne communiquez ce code qu'au livreur EcoDeli présent à votre domicile. 
                        En cas de problème, contactez-nous immédiatement.</p>
                    </div>

                    <div class="footer">
                        <p>Merci de faire confiance à <strong>EcoDeli</strong> pour vos livraisons éco-responsables !</p>
                        <p style="margin-top: 15px;">
                            <strong>L'équipe EcoDeli</strong> 🌱
                        </p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">
                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.<br>
                            © 2025 EcoDeli - Tous droits réservés
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                livraison.getDestinataire().getPrenom(),
                code,
                livraison.getIdLivraison(),
                livraison.getExpediteur().getPrenom(),
                livraison.getExpediteur().getNom(),
                livraison.getAdresseDeLivraison(),
                livraison.getCodePostalLivraison(),
                livraison.getColis() != null ?
                    "<p><strong>Colis :</strong> " + livraison.getColis().getDescription() + "</p>" : ""
            );
    }

    private String buildDeliveryCodeDriverEmailContent(Livraison livraison, String code) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Code de validation EcoDeli - Dépôt entrepôt</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .container {
                        background-color: #ffffff;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #2196F3;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #4CAF50;
                        margin-bottom: 10px;
                    }
                    .code-section {
                        background: linear-gradient(135deg, #2196F3, #1976D2);
                        color: white;
                        padding: 25px;
                        border-radius: 10px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .code {
                        font-size: 36px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        margin: 15px 0;
                        background-color: rgba(255,255,255,0.2);
                        padding: 15px;
                        border-radius: 8px;
                    }
                    .content {
                        font-size: 16px;
                        margin-bottom: 20px;
                    }
                    .delivery-info {
                        background-color: #E3F2FD;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #2196F3;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        font-size: 14px;
                        color: #666;
                        text-align: center;
                    }
                    .emoji {
                        font-size: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Espace livreur</p>
                    </div>

                    <h1 style="color: #1565C0; font-size: 24px; margin-bottom: 20px;">
                        Validation dépôt entrepôt <span class="emoji">🏢</span>
                    </h1>

                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>

                        <p>Vous êtes arrivé à l'entrepôt pour déposer le colis. Voici votre code de validation 
                        pour confirmer le dépôt du segment 1 :</p>
                    </div>

                    <div class="code-section">
                        <p style="margin: 0; font-size: 18px;">🔑 CODE DE VALIDATION</p>
                        <div class="code">%s</div>
                        <p style="margin: 0; font-size: 14px; opacity: 0.9;">Saisissez ce code dans votre application</p>
                    </div>

                    <div class="delivery-info">
                        <h3 style="color: #1565C0; margin-top: 0;">📦 Détails du segment 1</h3>
                        <p><strong>Livraison :</strong> #%d</p>
                        <p><strong>Entrepôt :</strong> %s</p>
                        <p><strong>Destinataire final :</strong> %s %s</p>
                        <p><strong>Adresse finale :</strong> %s, %s</p>
                        %s
                    </div>

                    <div class="content">
                        <p><strong>✅ Prochaine étape :</strong> Une fois le code saisi, le livreur du segment 2 
                        sera automatiquement notifié pour collecter le colis.</p>
                    </div>

                    <div class="footer">
                        <p>Merci de votre engagement pour des livraisons éco-responsables !</p>
                        <p style="margin-top: 15px;">
                            <strong>L'équipe EcoDeli</strong> 🌱
                        </p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">
                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.<br>
                            © 2025 EcoDeli - Tous droits réservés
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                livraison.getAnnonce().getLivreur().getPrenom(),
                code,
                livraison.getIdLivraison(),
                livraison.getEntrepotVille(),
                livraison.getDestinataire().getPrenom(),
                livraison.getDestinataire().getNom(),
                livraison.getAdresseDeLivraison(),
                livraison.getCodePostalLivraison(),
                livraison.getColis() != null ?
                    "<p><strong>Colis :</strong> " + livraison.getColis().getDescription() + "</p>" : ""
            );
    }

    private String buildSegment2NotificationEmailContent(Livraison livraison) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Colis prêt pour collecte</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .container {
                        background-color: #ffffff;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #4CAF50;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #4CAF50;
                        margin-bottom: 10px;
                    }
                    .notification-section {
                        background: linear-gradient(135deg, #4CAF50, #388E3C);
                        color: white;
                        padding: 25px;
                        border-radius: 10px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .content {
                        font-size: 16px;
                        margin-bottom: 20px;
                    }
                    .delivery-info {
                        background-color: #E8F5E8;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #4CAF50;
                        margin: 20px 0;
                    }
                    .cta-button {
                        display: inline-block;
                        background-color: #4CAF50;
                        color: white;
                        padding: 15px 30px;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        font-size: 14px;
                        color: #666;
                        text-align: center;
                    }
                    .emoji {
                        font-size: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Espace livreur</p>
                    </div>

                    <h1 style="color: #2E7D32; font-size: 24px; margin-bottom: 20px;">
                        Colis prêt pour collecte ! <span class="emoji">📦</span>
                    </h1>

                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>

                        <p>Excellente nouvelle ! Un colis est maintenant disponible pour collecte et livraison finale.</p>
                    </div>

                    <div class="notification-section">
                        <h2 style="margin: 0; font-size: 20px;">🎯 Segment 2 disponible</h2>
                        <p style="margin: 10px 0; font-size: 16px; opacity: 0.9;">
                            Le segment 1 a été complété avec succès
                        </p>
                    </div>

                    <div class="delivery-info">
                        <h3 style="color: #2E7D32; margin-top: 0;">📋 Détails de la livraison</h3>
                        <p><strong>Livraison :</strong> #%d</p>
                        <p><strong>À collecter :</strong> Entrepôt %s</p>
                        <p><strong>À livrer chez :</strong> %s %s</p>
                        <p><strong>Adresse de livraison :</strong> %s, %s</p>
                        <p><strong>Rémunération segment 2 :</strong> %d €</p>
                        %s
                    </div>

                    <div class="content">
                        <p><strong>🚀 Prochaines étapes :</strong></p>
                        <ol>
                            <li>Connectez-vous à votre espace livreur EcoDeli</li>
                            <li>Accédez à la section "Mes Segments"</li>
                            <li>Démarrez le segment 2 quand vous êtes prêt</li>
                        </ol>
                    </div>

                    <div style="text-align: center;">
                        <a href="#" class="cta-button">Accéder à mon espace livreur</a>
                    </div>

                    <div class="footer">
                        <p>Merci de contribuer à des livraisons plus durables !</p>
                        <p style="margin-top: 15px;">
                            <strong>L'équipe EcoDeli</strong> 🌱
                        </p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">
                            Cet email a été envoyé automatiquement, merci de ne pas y répondre.<br>
                            © 2025 EcoDeli - Tous droits réservés
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                livraison.getLivreurSegment2().getPrenom(),
                livraison.getIdLivraison(),
                livraison.getEntrepotVille(),
                livraison.getDestinataire().getPrenom(),
                livraison.getDestinataire().getNom(),
                livraison.getAdresseDeLivraison(),
                livraison.getCodePostalLivraison(),
                livraison.getPrix() / 2,
                livraison.getColis() != null ?
                    "<p><strong>Colis :</strong> " + livraison.getColis().getDescription() + "</p>" : ""
            );
    }
}
