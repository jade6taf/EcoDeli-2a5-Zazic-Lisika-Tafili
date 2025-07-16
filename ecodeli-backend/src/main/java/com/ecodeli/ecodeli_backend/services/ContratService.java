package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.models.ContratCommercant.StatutContrat;
import com.ecodeli.ecodeli_backend.repositories.ContratCommercantRepository;
import com.ecodeli.ecodeli_backend.repositories.TemplateContratRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ContratService {

    @Autowired
    private ContratCommercantRepository contratRepository;
    
    @Autowired
    private TemplateContratRepository templateRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private EmailService emailService;

    @PostConstruct
    private void initDefaultTemplateOnStartup() {
        // initDefaultTemplate();
    }
    
    public void initDefaultTemplate() {
        try {
            if (templateRepository.findByNomTemplate("Template Par Défaut").isPresent()) {
                return;
            }

            String contenuTemplate = chargerTemplateDefaut();
            
            TemplateContrat templateDefaut = new TemplateContrat();
            templateDefaut.setNomTemplate("Template Par Défaut");
            templateDefaut.setContenuTemplate(contenuTemplate);
            templateDefaut.setDescription("Template de contrat par défaut pour les nouveaux partenaires EcoDeli");
            templateDefaut.setActif(true);
            templateDefaut.setDateCreation(LocalDateTime.now());
            templateDefaut.setAdminCreateur(null);
            
            templateRepository.save(templateDefaut);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du template par défaut : " + e.getMessage());
        }
    }
    
    private String chargerTemplateDefaut() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("templates/contrat-default.html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return createBasicTemplate();
        }
    }
    
    private String createBasicTemplate() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;">
                <div style="text-align: center; margin-bottom: 40px; border-bottom: 2px solid #4CAF50; padding-bottom: 20px;">
                    <h1 style="color: #4CAF50; margin-bottom: 10px;">🌱 EcoDeli</h1>
                    <h2>Contrat de Partenariat Commercial</h2>
                    <p>Plateforme de livraison éco-responsable</p>
                </div>

                <div style="margin-bottom: 30px;">
                    <h3 style="color: #2e7d32; border-left: 4px solid #4CAF50; padding-left: 15px;">Informations du Partenaire</h3>
                    <div style="background-color: #f9f9f9; border: 1px solid #ddd; border-radius: 5px; padding: 15px; margin-bottom: 20px;">
                        <p><strong>Nom du commerce :</strong> {{NOM_COMMERCE}}</p>
                        <p><strong>Représentant légal :</strong> {{PRENOM_COMMERCANT}} {{NOM_COMMERCANT}}</p>
                        <p><strong>Email de contact :</strong> {{EMAIL_COMMERCANT}}</p>
                        <p><strong>Téléphone :</strong> {{TELEPHONE_COMMERCANT}}</p>
                        <p><strong>SIRET :</strong> {{SIRET}}</p>
                    </div>
                </div>

                <div style="margin-bottom: 30px;">
                    <h3 style="color: #2e7d32; border-left: 4px solid #4CAF50; padding-left: 15px;">Article 1 - Objet du Contrat</h3>
                    <p>Le présent contrat a pour objet de définir les conditions de partenariat entre <strong>{{NOM_COMMERCE}}</strong> (ci-après dénommé "le Partenaire") et <strong>EcoDeli</strong> (ci-après dénommé "la Plateforme") pour la mise à disposition de services de livraison éco-responsable.</p>
                </div>

                <div style="margin-bottom: 30px;">
                    <h3 style="color: #2e7d32; border-left: 4px solid #4CAF50; padding-left: 15px;">Article 2 - Services Proposés</h3>
                    <p>EcoDeli s'engage à fournir au Partenaire :</p>
                    <ul>
                        <li>Une plateforme en ligne pour la gestion des commandes</li>
                        <li>Un réseau de livreurs éco-responsables (vélos, véhicules électriques)</li>
                        <li>Un système de suivi en temps réel des livraisons</li>
                        <li>Un support technique et commercial</li>
                        <li>Des outils de reporting et d'analyse</li>
                    </ul>
                </div>

                <div style="margin-bottom: 30px;">
                    <h3 style="color: #2e7d32; border-left: 4px solid #4CAF50; padding-left: 15px;">Article 3 - Engagements du Partenaire</h3>
                    <p>Le Partenaire s'engage à :</p>
                    <ul>
                        <li>Respecter les valeurs éco-responsables d'EcoDeli</li>
                        <li>Fournir des informations exactes sur ses produits et services</li>
                        <li>Préparer les commandes dans les délais convenus</li>
                        <li>Respecter les standards de qualité et d'hygiène</li>
                        <li>Utiliser prioritairement des emballages recyclables</li>
                    </ul>
                </div>

                <div style="margin-top: 50px; border-top: 1px solid #ddd; padding-top: 30px;">
                    <h3 style="color: #2e7d32; border-left: 4px solid #4CAF50; padding-left: 15px;">Signatures</h3>
                    <div style="display: flex; justify-content: space-between; margin-top: 30px;">
                        <div style="width: 45%;">
                            <p><strong>Le Partenaire</strong></p>
                            <p>{{PRENOM_COMMERCANT}} {{NOM_COMMERCANT}}</p>
                            <p>{{NOM_COMMERCE}}</p>
                            <p>Date : {{DATE_AUJOURDHUI}}</p>
                            <div style="border: 1px solid #ddd; height: 80px; margin: 10px 0; background-color: #fafafa;"></div>
                            <p style="text-align: center; margin-top: 10px;"><em>Signature</em></p>
                        </div>
                        <div style="width: 45%;">
                            <p><strong>EcoDeli</strong></p>
                            <p>Représentant légal</p>
                            <p>Date : {{DATE_AUJOURDHUI}}</p>
                            <div style="border: 1px solid #ddd; height: 80px; margin: 10px 0; background-color: #fafafa;"></div>
                            <p style="text-align: center; margin-top: 10px;"><em>Signature</em></p>
                        </div>
                    </div>
                </div>

                <div style="margin-top: 40px; text-align: center; font-size: 0.9em; color: #666;">
                    <p>EcoDeli - Plateforme de livraison éco-responsable</p>
                    <p>Contact : ecodeli.nepasrepondre@gmail.com | Tél : 01 23 45 67 89</p>
                    <p><em>Ensemble, construisons un avenir plus vert ! 🌱</em></p>
                </div>
            </div>
            """;
    }

    public ContratCommercant demanderContrat(Integer idCommercant) {
        Commercant commercant = (Commercant) utilisateurRepository.findById(idCommercant)
            .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé"));
        
        Optional<ContratCommercant> contratExistant = contratRepository.findByCommercant_IdUtilisateur(idCommercant);
        if (contratExistant.isPresent()) {
            throw new RuntimeException("Une demande de contrat existe déjà pour ce commerçant");
        }
        
        ContratCommercant contrat = new ContratCommercant();
        contrat.setCommercant(commercant);
        contrat.setStatut(StatutContrat.DEMANDE_ENVOYEE);
        contrat.setDateDemande(LocalDateTime.now());
        
        ContratCommercant savedContrat = contratRepository.save(contrat);
        
        envoyerNotificationDemandeContrat(commercant);
        
        return savedContrat;
    }
    
    public ContratCommercant getContratByCommercant(Integer idCommercant) {
        return contratRepository.findByCommercant_IdUtilisateur(idCommercant)
            .orElse(null);
    }
    
    public ContratCommercant signerContrat(Integer idContrat, Integer idCommercant) {
        ContratCommercant contrat = contratRepository.findById(idContrat)
            .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));
        
        if (!contrat.getCommercant().getIdUtilisateur().equals(idCommercant)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à signer ce contrat");
        }
        
        if (contrat.getStatut() != StatutContrat.CONTRAT_CREE) {
            throw new RuntimeException("Ce contrat ne peut pas être signé dans son état actuel");
        }
        
        contrat.setSignatureCommercant(true);
        contrat.setDateSignature(LocalDateTime.now());
        contrat.setStatut(StatutContrat.SIGNE_VALIDE);
        
        return contratRepository.save(contrat);
    }

    public List<ContratCommercant> getAllContrats() {
        return contratRepository.findAllByOrderByDateDemandeDesc();
    }
    
    public List<ContratCommercant> getContratsByStatut(StatutContrat statut) {
        return contratRepository.findByStatutOrderByDateDemandeDesc(statut);
    }
    
    public ContratCommercant getContratById(Integer idContrat) {
        return contratRepository.findById(idContrat)
            .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));
    }
    
    public ContratCommercant creerContrat(Integer idContrat, String contenuContrat, Integer idAdmin, String commentaire) {
        ContratCommercant contrat = getContratById(idContrat);
        Admin admin = (Admin) utilisateurRepository.findById(idAdmin)
            .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));
        
        contrat.setContenuContrat(contenuContrat);
        contrat.setAdminCreateur(admin);
        contrat.setCommentaireAdmin(commentaire);
        contrat.setDateCreationContrat(LocalDateTime.now());
        contrat.setStatut(StatutContrat.CONTRAT_CREE);
        
        ContratCommercant savedContrat = contratRepository.save(contrat);
        
        envoyerNotificationContratCree(contrat);
        
        return savedContrat;
    }
    
    public ContratCommercant mettreAJourStatut(Integer idContrat, StatutContrat nouveauStatut, String commentaire) {
        ContratCommercant contrat = getContratById(idContrat);
        
        contrat.setStatut(nouveauStatut);
        if (commentaire != null && !commentaire.isEmpty()) {
            contrat.setCommentaireAdmin(commentaire);
        }
        
        return contratRepository.save(contrat);
    }
    
    public void supprimerContrat(Integer idContrat) {
        if (!contratRepository.existsById(idContrat)) {
            throw new ResourceNotFoundException("Contrat non trouvé");
        }
        contratRepository.deleteById(idContrat);
    }

    public List<TemplateContrat> getAllTemplates() {
        return templateRepository.findAllByOrderByDateCreationDesc();
    }
    
    public List<TemplateContrat> getTemplatesActifs() {
        return templateRepository.findByActifTrueOrderByDateCreationDesc();
    }
    
    public TemplateContrat getTemplateById(Integer idTemplate) {
        return templateRepository.findById(idTemplate)
            .orElseThrow(() -> new ResourceNotFoundException("Template non trouvé"));
    }
    
    public TemplateContrat creerTemplate(String nomTemplate, String contenuTemplate, String description, Integer idAdmin) {
        if (templateRepository.existsByNomTemplate(nomTemplate)) {
            throw new RuntimeException("Un template avec ce nom existe déjà");
        }
        
        Admin admin = (Admin) utilisateurRepository.findById(idAdmin)
            .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));
        
        TemplateContrat template = new TemplateContrat();
        template.setNomTemplate(nomTemplate);
        template.setContenuTemplate(contenuTemplate);
        template.setDescription(description);
        template.setAdminCreateur(admin);
        template.setDateCreation(LocalDateTime.now());
        template.setActif(true);
        
        return templateRepository.save(template);
    }
    
    public TemplateContrat mettreAJourTemplate(Integer idTemplate, Map<String, Object> updates) {
        TemplateContrat template = getTemplateById(idTemplate);
        
        if (updates.containsKey("nomTemplate")) {
            String nouveauNom = (String) updates.get("nomTemplate");
            if (!template.getNomTemplate().equals(nouveauNom) && templateRepository.existsByNomTemplate(nouveauNom)) {
                throw new RuntimeException("Un template avec ce nom existe déjà");
            }
            template.setNomTemplate(nouveauNom);
        }
        
        if (updates.containsKey("contenuTemplate")) {
            template.setContenuTemplate((String) updates.get("contenuTemplate"));
        }
        
        if (updates.containsKey("description")) {
            template.setDescription((String) updates.get("description"));
        }
        
        if (updates.containsKey("actif")) {
            template.setActif((Boolean) updates.get("actif"));
        }
        
        template.setDateModification(LocalDateTime.now());
        
        return templateRepository.save(template);
    }
    
    public void supprimerTemplate(Integer idTemplate) {
        if (!templateRepository.existsById(idTemplate)) {
            throw new ResourceNotFoundException("Template non trouvé");
        }
        templateRepository.deleteById(idTemplate);
    }

    public Map<String, Long> getStatistiquesContrats() {
        return Map.of(
            "totalContrats", contratRepository.count(),
            "demandesEnvoyees", contratRepository.countByStatut(StatutContrat.DEMANDE_ENVOYEE),
            "enAttenteAdmin", contratRepository.countByStatut(StatutContrat.EN_ATTENTE_ADMIN),
            "contratsCreés", contratRepository.countByStatut(StatutContrat.CONTRAT_CREE),
            "signesValides", contratRepository.countByStatut(StatutContrat.SIGNE_VALIDE),
            "refuses", contratRepository.countByStatut(StatutContrat.REFUSE)
        );
    }

    private void envoyerNotificationDemandeContrat(Commercant commercant) {
        try {
            String subject = "🏪 Nouvelle demande de contrat commerçant - " + commercant.getNomCommerce();
            String content = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;\">" +
                "<div style=\"background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"margin: 0; font-size: 24px;\">🌱 EcoDeli Admin</h1>" +
                "<p style=\"margin: 5px 0 0 0; opacity: 0.9;\">Nouvelle demande de contrat</p>" +
                "</div>" +
                "<div style=\"background: white; padding: 30px; border-radius: 0 0 10px 10px;\">" +
                "<h2 style=\"color: #333; margin-top: 0;\">Nouvelle demande de contrat reçue</h2>" +
                "<p style=\"color: #666;\"><strong>Commerçant :</strong> %s %s</p>" +
                "<p style=\"color: #666;\"><strong>Commerce :</strong> %s</p>" +
                "<p style=\"color: #666;\"><strong>Email :</strong> %s</p>" +
                "<p style=\"color: #666;\"><strong>Téléphone :</strong> %s</p>" +
                "<div style=\"background: #e8f5e8; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;\">" +
                "<p style=\"margin: 0; color: #2e7d32;\"><strong>Action requise :</strong> Connectez-vous à l'interface d'administration pour traiter cette demande.</p>" +
                "</div>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5174/admin/contrats\" style=\"background: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;\">Gérer les contrats</a>" +
                "</div>" +
                "</div>" +
                "</div>",
                commercant.getPrenom(), commercant.getNom(), commercant.getNomCommerce(), 
                commercant.getEmail(), commercant.getTelephone() != null ? commercant.getTelephone() : "Non renseigné"
            );
            
            emailService.sendHtmlEmail("admin@ecodeli.fr", subject, content);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification admin : " + e.getMessage());
        }
    }
    
    private void envoyerNotificationContratCree(ContratCommercant contrat) {
        try {
            String subject = "📋 Votre contrat EcoDeli est prêt à être signé";
            String content = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px;\">" +
                "<div style=\"background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"margin: 0; font-size: 24px;\">🌱 EcoDeli</h1>" +
                "<p style=\"margin: 5px 0 0 0; opacity: 0.9;\">Contrat prêt à signer</p>" +
                "</div>" +
                "<div style=\"background: white; padding: 30px; border-radius: 0 0 10px 10px;\">" +
                "<h2 style=\"color: #333; margin-top: 0;\">Bonjour %s,</h2>" +
                "<p style=\"color: #666;\">Votre contrat EcoDeli pour <strong>%s</strong> a été créé et est maintenant prêt à être signé.</p>" +
                "<div style=\"background: #e8f5e8; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;\">" +
                "<p style=\"margin: 0; color: #2e7d32;\"><strong>Prochaine étape :</strong> Connectez-vous à votre espace commerçant pour consulter et signer votre contrat.</p>" +
                "</div>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/commercant\" style=\"background: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;\">Signer mon contrat</a>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 12px; margin-top: 30px;\">Si vous avez des questions, n'hésitez pas à nous contacter.</p>" +
                "</div>" +
                "</div>",
                contrat.getCommercant().getPrenom(), contrat.getCommercant().getNomCommerce()
            );
            
            emailService.sendHtmlEmail(contrat.getCommercant().getEmail(), subject, content);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification commerçant : " + e.getMessage());
        }
    }
}