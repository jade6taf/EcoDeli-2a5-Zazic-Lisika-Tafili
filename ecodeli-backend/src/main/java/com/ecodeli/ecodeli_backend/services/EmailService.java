package com.ecodeli.ecodeli_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.RetraitDemande;

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

    public void sendDeliveryConfirmationToSender(Livraison livraison) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(livraison.getExpediteur().getEmail());
            helper.setSubject("EcoDeli - Livraison terminée - Livraison #" + livraison.getIdLivraison());

            String htmlContent = buildDeliveryConfirmationSenderEmailContent(livraison);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Confirmation de livraison envoyée à l'expéditeur {} pour la livraison #{}",
                       livraison.getExpediteur().getEmail(), livraison.getIdLivraison());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la confirmation à l'expéditeur {} : {}",
                        livraison.getExpediteur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de la confirmation à l'expéditeur {} : {}",
                        livraison.getExpediteur().getEmail(), e.getMessage());
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

    private String buildDeliveryConfirmationSenderEmailContent(Livraison livraison) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Confirmation de livraison</title>
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
                    .success-section {
                        background: linear-gradient(135deg, #4CAF50, #388E3C);
                        color: white;
                        padding: 25px;
                        border-radius: 10px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .success-icon {
                        font-size: 48px;
                        margin-bottom: 15px;
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
                    .rating-section {
                        background-color: #FFF9C4;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #FFC107;
                        margin: 20px 0;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Livraison éco-responsable</p>
                    </div>

                    <h1 style="color: #2E7D32; font-size: 24px; margin-bottom: 20px;">
                        Bonjour %s ! <span class="emoji">👋</span>
                    </h1>

                    <div class="success-section">
                        <div class="success-icon">✅</div>
                        <h2 style="margin: 0; font-size: 24px;">Livraison terminée avec succès !</h2>
                        <p style="margin: 10px 0; font-size: 16px; opacity: 0.9;">
                            Livraison #%d
                        </p>
                    </div>

                    <div class="content">
                        <p>Votre colis a été livré avec succès chez le destinataire.</p>
                    </div>

                    <div class="delivery-info">
                        <h3 style="color: #2E7D32; margin-top: 0;">📋 Récapitulatif de votre livraison</h3>
                        <p><strong>Livraison :</strong> #%d</p>
                        <p><strong>Destinataire :</strong> %s %s</p>
                        <p><strong>Adresse de livraison :</strong> %s, %s</p>
                        <p><strong>Type de livraison :</strong> Livraison directe</p>
                        %s
                        %s
                    </div>

                    <div class="content">
                        <p>Votre livraison est maintenant complètement terminée. Merci d'avoir choisi EcoDeli !</p>
                    </div>

                    <div class="rating-section">
                        <h3 style="color: #F57C00; margin-top: 0;">⭐ Votre avis nous intéresse</h3>
                        <p>Comment s'est passée votre expérience EcoDeli ? Votre retour nous aide à améliorer nos services.</p>
                        <p style="margin-top: 15px;">
                            <a href="#" style="color: #4CAF50; text-decoration: none; font-weight: bold;">Donner mon avis →</a>
                        </p>
                    </div>

                    <div class="content">
                        <p>Merci d'avoir choisi <strong>EcoDeli</strong> pour cette livraison éco-responsable ! 
                        Ensemble, nous contribuons à un avenir plus durable.</p>
                    </div>

                    <div class="footer">
                        <p>Vos livraisons vertes font la différence ! 🌍</p>
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
                livraison.getExpediteur().getPrenom(),
                livraison.getIdLivraison(),
                livraison.getIdLivraison(),
                livraison.getDestinataire().getPrenom(),
                livraison.getDestinataire().getNom(),
                livraison.getAdresseDeLivraison(),
                livraison.getCodePostalLivraison(),
                livraison.getColis() != null ?
                    "<p><strong>Colis :</strong> " + livraison.getColis().getDescription() + "</p>" : "",
                livraison.getDateFin() != null ?
                    "<p><strong>Date de livraison :</strong> " + livraison.getDateFin().toString() + "</p>" : ""
            );
    }

    public void sendWithdrawalRequestConfirmation(RetraitDemande retrait) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(retrait.getLivreur().getEmail());
            helper.setSubject("EcoDeli - Demande de retrait confirmée 💰");

            String htmlContent = buildWithdrawalRequestEmailContent(retrait);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de confirmation de retrait envoyé à {} pour {}€",
                       retrait.getLivreur().getEmail(), retrait.getMontant());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de confirmation de retrait à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email de confirmation de retrait à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        }
    }

    public void sendWithdrawalCompletedNotification(RetraitDemande retrait) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(retrait.getLivreur().getEmail());
            helper.setSubject("EcoDeli - Virement effectué ✅");

            String htmlContent = buildWithdrawalCompletedEmailContent(retrait);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de virement effectué envoyé à {} pour {}€",
                       retrait.getLivreur().getEmail(), retrait.getMontant());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de virement effectué à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email de virement effectué à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        }
    }

    public void sendWithdrawalFailedNotification(RetraitDemande retrait) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(retrait.getLivreur().getEmail());
            helper.setSubject("EcoDeli - Problème avec votre retrait ⚠️");

            String htmlContent = buildWithdrawalFailedEmailContent(retrait);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email d'échec de retrait envoyé à {} pour {}€",
                       retrait.getLivreur().getEmail(), retrait.getMontant());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email d'échec de retrait à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email d'échec de retrait à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        }
    }

    public void sendFictiveWithdrawalConfirmation(RetraitDemande retrait) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EcoDeli");
            helper.setTo(retrait.getLivreur().getEmail());
            helper.setSubject("EcoDeli - Retrait traité ✅ " + retrait.getMontant() + "€");

            String htmlContent = buildFictiveWithdrawalEmailContent(retrait);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de retrait fictif confirmé envoyé à {} pour {}€",
                       retrait.getLivreur().getEmail(), retrait.getMontant());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de retrait fictif à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email de retrait fictif à {} : {}",
                        retrait.getLivreur().getEmail(), e.getMessage());
        }
    }

    private String buildWithdrawalRequestEmailContent(RetraitDemande retrait) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Demande de retrait confirmée</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .container { background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #4CAF50; margin-bottom: 30px; }
                    .logo { font-size: 32px; font-weight: bold; color: #4CAF50; margin-bottom: 10px; }
                    .amount-section { background: linear-gradient(135deg, #4CAF50, #388E3C); color: white; padding: 25px; border-radius: 10px; text-align: center; margin: 25px 0; }
                    .amount { font-size: 36px; font-weight: bold; margin: 15px 0; }
                    .info-section { background-color: #E8F5E8; padding: 20px; border-radius: 8px; border-left: 4px solid #4CAF50; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; font-size: 14px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Portefeuille livreur</p>
                    </div>

                    <h1 style="color: #2E7D32; font-size: 24px; margin-bottom: 20px;">
                        Bonjour %s ! 👋
                    </h1>

                    <div class="amount-section">
                        <p style="margin: 0; font-size: 18px;">💰 DEMANDE DE RETRAIT CONFIRMÉE</p>
                        <div class="amount">%s €</div>
                        <p style="margin: 0; font-size: 14px; opacity: 0.9;">Demande #%d</p>
                    </div>

                    <div style="font-size: 16px; margin-bottom: 20px;">
                        <p>Votre demande de retrait a été enregistrée avec succès !</p>
                        <p>Nos équipes vont traiter votre demande dans les plus brefs délais. Vous recevrez un email de confirmation une fois le virement effectué.</p>
                    </div>

                    <div class="info-section">
                        <h3 style="color: #2E7D32; margin-top: 0;">📋 Détails de votre demande</h3>
                        <p><strong>Montant demandé :</strong> %s €</p>
                        <p><strong>Compte de destination :</strong> %s</p>
                        <p><strong>Titulaire :</strong> %s</p>
                        <p><strong>Date de demande :</strong> %s</p>
                        <p><strong>Statut :</strong> En attente de traitement</p>
                    </div>

                    <div style="font-size: 16px; margin-bottom: 20px;">
                        <p><strong>💡 Bon à savoir :</strong></p>
                        <ul>
                            <li>Le virement sera effectué sous 1-2 jours ouvrés</li>
                            <li>Aucun frais ne sera prélevé par EcoDeli</li>
                            <li>Vous pouvez annuler votre demande tant qu'elle n'est pas traitée</li>
                        </ul>
                    </div>

                    <div class="footer">
                        <p>Merci d'être livreur EcoDeli ! 🚴‍♂️</p>
                        <p style="margin-top: 15px;"><strong>L'équipe EcoDeli</strong> 🌱</p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">© 2025 EcoDeli - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                retrait.getLivreur().getPrenom(),
                retrait.getMontant(),
                retrait.getIdRetrait(),
                retrait.getMontant(),
                retrait.getIban().substring(0, 4) + "****" + retrait.getIban().substring(retrait.getIban().length() - 4),
                retrait.getNomTitulaire(),
                retrait.getDateDemande().toString()
            );
    }

    private String buildWithdrawalCompletedEmailContent(RetraitDemande retrait) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Virement effectué</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .container { background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #4CAF50; margin-bottom: 30px; }
                    .logo { font-size: 32px; font-weight: bold; color: #4CAF50; margin-bottom: 10px; }
                    .success-section { background: linear-gradient(135deg, #4CAF50, #388E3C); color: white; padding: 25px; border-radius: 10px; text-align: center; margin: 25px 0; }
                    .success-icon { font-size: 48px; margin-bottom: 15px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; font-size: 14px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                    </div>

                    <div class="success-section">
                        <div class="success-icon">✅</div>
                        <h2 style="margin: 0; font-size: 24px;">Virement effectué avec succès !</h2>
                        <p style="margin: 10px 0; font-size: 18px;">%s €</p>
                    </div>

                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Votre retrait de <strong>%s €</strong> a été traité et le virement a été effectué vers votre compte bancaire.</p>
                    <p>Les fonds devraient apparaître sur votre compte dans les prochaines heures selon votre banque.</p>

                    <div class="footer">
                        <p>Merci d'être livreur EcoDeli ! 🚴‍♂️</p>
                        <p><strong>L'équipe EcoDeli</strong> 🌱</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                retrait.getMontant(),
                retrait.getLivreur().getPrenom(),
                retrait.getMontant()
            );
    }

    private String buildWithdrawalFailedEmailContent(RetraitDemande retrait) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Problème avec votre retrait</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .container { background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #f44336; margin-bottom: 30px; }
                    .logo { font-size: 32px; font-weight: bold; color: #4CAF50; margin-bottom: 10px; }
                    .error-section { background: linear-gradient(135deg, #f44336, #d32f2f); color: white; padding: 25px; border-radius: 10px; text-align: center; margin: 25px 0; }
                    .error-icon { font-size: 48px; margin-bottom: 15px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; font-size: 14px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                    </div>

                    <div class="error-section">
                        <div class="error-icon">⚠️</div>
                        <h2 style="margin: 0; font-size: 24px;">Problème avec votre retrait</h2>
                        <p style="margin: 10px 0; font-size: 18px;">%s €</p>
                    </div>

                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Nous rencontrons un problème avec votre demande de retrait de <strong>%s €</strong>.</p>
                    <p><strong>Raison :</strong> %s</p>
                    <p>Votre solde a été remis à disposition dans votre portefeuille. Vous pouvez effectuer une nouvelle demande de retrait en vérifiant vos informations bancaires.</p>

                    <div class="footer">
                        <p>L'équipe EcoDeli est à votre disposition pour vous aider.</p>
                        <p><strong>Support EcoDeli</strong> 🌱</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                retrait.getMontant(),
                retrait.getLivreur().getPrenom(),
                retrait.getMontant(),
                retrait.getMotifRefus() != null ? retrait.getMotifRefus() : "Erreur technique"
            );
    }

    private String buildFictiveWithdrawalEmailContent(RetraitDemande retrait) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>EcoDeli - Retrait traité</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; }
                    .container { background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #4CAF50; margin-bottom: 30px; }
                    .logo { font-size: 32px; font-weight: bold; color: #4CAF50; margin-bottom: 10px; }
                    .success-section { background: linear-gradient(135deg, #4CAF50, #388E3C); color: white; padding: 25px; border-radius: 10px; text-align: center; margin: 25px 0; }
                    .success-icon { font-size: 48px; margin-bottom: 15px; }
                    .info-section { background-color: #E3F2FD; padding: 20px; border-radius: 8px; border-left: 4px solid #2196F3; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; font-size: 14px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🌱 EcoDeli</div>
                        <p style="color: #666; margin: 0;">Portefeuille livreur</p>
                    </div>

                    <h1 style="color: #2E7D32; font-size: 24px; margin-bottom: 20px;">
                        Bonjour %s ! 👋
                    </h1>

                    <div class="success-section">
                        <div class="success-icon">✅</div>
                        <h2 style="margin: 0; font-size: 24px;">Retrait traité avec succès !</h2>
                        <p style="margin: 10px 0; font-size: 18px;">%s €</p>
                        <p style="margin: 0; font-size: 14px; opacity: 0.9;">Référence : %s</p>
                    </div>

                    <div style="font-size: 16px; margin-bottom: 20px;">
                        <p>Votre demande de retrait de <strong>%s €</strong> a été traitée avec succès !</p>
                        <p>Les fonds ont été déduits de votre wallet EcoDeli et votre demande est maintenant complète.</p>
                    </div>

                    <div class="info-section">
                        <h3 style="color: #1976D2; margin-top: 0;">📋 Détails du retrait</h3>
                        <p><strong>Montant :</strong> %s €</p>
                        <p><strong>Compte de destination :</strong> ****%s</p>
                        <p><strong>Titulaire :</strong> %s</p>
                        <p><strong>Date de traitement :</strong> %s</p>
                        <p><strong>Type :</strong> Retrait EcoDeli</p>
                        <p><strong>Référence :</strong> %s</p>
                    </div>

                    <div style="font-size: 16px; margin-bottom: 20px;">
                        <p><strong>💡 Note importante :</strong></p>
                        <p>Il s'agit d'un retrait virtuel dans le système EcoDeli. Les fonds ont été déduits de votre wallet numérique et cette transaction est maintenant enregistrée dans votre historique.</p>
                    </div>

                    <div style="background-color: #FFF3E0; padding: 15px; border-radius: 5px; border-left: 4px solid #FF9800; margin: 20px 0;">
                        <p style="margin: 0; color: #E65100;"><strong>🎯 Continuez à livrer !</strong></p>
                        <p style="margin: 5px 0 0 0;">Plus vous livrez, plus vous gagnez ! Consultez les annonces disponibles sur votre tableau de bord.</p>
                    </div>

                    <div class="footer">
                        <p>Merci d'être livreur EcoDeli ! 🚴‍♂️</p>
                        <p style="margin-top: 15px;"><strong>L'équipe EcoDeli</strong> 🌱</p>
                        <hr style="margin: 20px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999;">© 2025 EcoDeli - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                retrait.getLivreur().getPrenom(),
                retrait.getMontant(),
                retrait.getReferenceVirement(),
                retrait.getMontant(),
                retrait.getMontant(),
                retrait.getIban().substring(retrait.getIban().length() - 4),
                retrait.getNomTitulaire(),
                retrait.getDateTraitement() != null ? retrait.getDateTraitement().toString() : "Maintenant",
                retrait.getReferenceVirement()
            );
    }
}
