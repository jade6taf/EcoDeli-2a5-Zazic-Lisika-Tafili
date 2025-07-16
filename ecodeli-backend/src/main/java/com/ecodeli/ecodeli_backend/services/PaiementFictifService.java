package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaiementFictifService {

    @Autowired
    private PortefeuilleRepository portefeuilleRepository;

    @Autowired
    private TransactionPortefeuilleRepository transactionRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    private static final BigDecimal TAUX_COMMISSION = new BigDecimal("0.05");


    @Transactional
    public Map<String, Object> simulerPaiementMission(Long candidatureId, BigDecimal montantTotal) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Thread.sleep(1500);
            
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (!candidatureOpt.isPresent()) {
                throw new RuntimeException("Candidature non trouvée avec ID: " + candidatureId);
            }
            
            Candidature candidature = candidatureOpt.get();
            Prestataire prestataire = candidature.getPrestataire();
            
            BigDecimal commissionEcodeli = montantTotal.multiply(TAUX_COMMISSION).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montantPrestataire = montantTotal.subtract(commissionEcodeli);
            
            PortefeuillePrestataire portefeuille = getOrCreatePortefeuille(prestataire.getIdUtilisateur());
            
            TransactionPortefeuille transaction = new TransactionPortefeuille();
            transaction.setPortefeuille(portefeuille);
            transaction.setCandidature(candidature);
            transaction.setTypeTransaction(TransactionPortefeuille.TypeTransaction.CREDIT_MISSION);
            transaction.setMontant(montantPrestataire);
            transaction.setCommissionEcodeli(commissionEcodeli);
            transaction.setStatutTransaction(TransactionPortefeuille.StatutTransaction.EFFECTUE);
            transaction.setDateTransaction(LocalDateTime.now());
            transaction.setDateTraitement(LocalDateTime.now());
            transaction.setReferenceExterne(
                TransactionPortefeuille.genererReference(
                    TransactionPortefeuille.TypeTransaction.CREDIT_MISSION,
                    prestataire.getIdUtilisateur().longValue()
                )
            );
            
            String descriptionMission = candidature.getDemandeService() != null ? 
                candidature.getDemandeService().getTitre() : "Mission";
            transaction.setDescription("Mission validée - " + descriptionMission);
            
            transaction = transactionRepository.save(transaction);
            
            portefeuille.crediterMission(montantPrestataire);
            portefeuille = portefeuilleRepository.save(portefeuille);
            
            envoyerEmailCreditMission(prestataire, montantPrestataire, descriptionMission);
            
            result.put("success", true);
            result.put("message", "Paiement effectué avec succès");
            result.put("montantTotal", montantTotal);
            result.put("commissionEcodeli", commissionEcodeli);
            result.put("montantPrestataire", montantPrestataire);
            result.put("nouveauSolde", portefeuille.getSoldeDisponible());
            result.put("transactionId", transaction.getIdTransaction());
            result.put("reference", transaction.getReferenceExterne());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Interruption lors du traitement");
        } catch (Exception e) {
            System.err.println("Erreur simulation paiement: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }


    @Transactional
    public Map<String, Object> simulerRetraitPrestataire(Integer prestataireId, BigDecimal montantRetrait, String iban) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Thread.sleep(2500);
            
            Optional<PortefeuillePrestataire> portefeuilleOpt = portefeuilleRepository.findByPrestataireIdUtilisateur(prestataireId);
            if (!portefeuilleOpt.isPresent()) {
                throw new RuntimeException("Portefeuille non trouvé pour le prestataire ID: " + prestataireId);
            }
            
            PortefeuillePrestataire portefeuille = portefeuilleOpt.get();
            
            if (!portefeuille.peutRetirer(montantRetrait)) {
                throw new RuntimeException("Solde insuffisant pour effectuer ce retrait");
            }
            
            TransactionPortefeuille transaction = new TransactionPortefeuille();
            transaction.setPortefeuille(portefeuille);
            transaction.setTypeTransaction(TransactionPortefeuille.TypeTransaction.RETRAIT_PRESTATAIRE);
            transaction.setMontant(montantRetrait);
            transaction.setStatutTransaction(TransactionPortefeuille.StatutTransaction.EFFECTUE);
            transaction.setDateTransaction(LocalDateTime.now());
            transaction.setDateTraitement(LocalDateTime.now());
            transaction.setIbanDestinataire(iban);
            transaction.setReferenceExterne(
                TransactionPortefeuille.genererReference(
                    TransactionPortefeuille.TypeTransaction.RETRAIT_PRESTATAIRE,
                    prestataireId.longValue()
                )
            );
            transaction.setDescription("Retrait vers " + masquerIban(iban));
            
            transaction = transactionRepository.save(transaction);
            
            BigDecimal ancienSolde = portefeuille.getSoldeDisponible();
            portefeuille.debiterRetrait(montantRetrait);
            portefeuille = portefeuilleRepository.save(portefeuille);
            
            envoyerEmailRetraitEffectue(portefeuille.getPrestataire(), montantRetrait, iban);
            
            result.put("success", true);
            result.put("message", "Retrait effectué avec succès");
            result.put("montantRetrait", montantRetrait);
            result.put("ancienSolde", ancienSolde);
            result.put("nouveauSolde", portefeuille.getSoldeDisponible());
            result.put("ibanDestination", masquerIban(iban));
            result.put("transactionId", transaction.getIdTransaction());
            result.put("reference", transaction.getReferenceExterne());
            result.put("delaiVirement", "24-48h ouvrées");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Interruption lors du traitement");
        } catch (Exception e) {
            System.err.println("Erreur simulation retrait: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }


    public Map<String, Object> getPortefeuillePrestataire(Integer prestataireId) {
        PortefeuillePrestataire portefeuille = getOrCreatePortefeuille(prestataireId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("prestataireId", prestataireId);
        result.put("soldeDisponible", portefeuille.getSoldeDisponible());
        result.put("soldeEnAttente", portefeuille.getSoldeEnAttente());
        result.put("soldeTotal", portefeuille.getSoldeTotal());
        result.put("totalGagne", portefeuille.getTotalGagne());
        result.put("totalRetire", portefeuille.getTotalRetire());
        result.put("nombreTransactions", portefeuille.getNombreTransactions());
        result.put("ibanMasque", portefeuille.getIbanMasque());
        result.put("nomTitulaire", portefeuille.getNomTitulaire());
        result.put("dateCreation", portefeuille.getDateCreation());
        result.put("dateModification", portefeuille.getDateModification());
        
        return result;
    }


    public List<Map<String, Object>> getHistoriqueTransactions(Integer prestataireId, int limit) {
        List<TransactionPortefeuille> transactions = transactionRepository.findByPrestataireIdOrderByDateDesc(prestataireId);
        
        return transactions.stream()
            .limit(limit > 0 ? limit : transactions.size())
            .map(this::formatTransactionForResponse)
            .toList();
    }


    @Transactional
    public Map<String, Object> updateInfosBancaires(Integer prestataireId, String iban, String nomTitulaire) {
        PortefeuillePrestataire portefeuille = getOrCreatePortefeuille(prestataireId);
        portefeuille.updateInfosBancaires(iban, nomTitulaire);
        portefeuille = portefeuilleRepository.save(portefeuille);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Informations bancaires mises à jour");
        result.put("ibanMasque", portefeuille.getIbanMasque());
        result.put("nomTitulaire", portefeuille.getNomTitulaire());
        
        return result;
    }


    public Map<String, Object> getTotalDepenseClient(Integer clientId) {
        try {
            List<Candidature> candidaturesPaye = candidatureRepository.findAll().stream()
                .filter(c -> c.getDemandeService() != null &&
                           c.getDemandeService().getClient() != null &&
                           c.getDemandeService().getClient().getIdUtilisateur().equals(clientId) &&
                           c.getStatut() == Candidature.StatutCandidature.ACCEPTEE)
                .toList();
            
            BigDecimal totalServices = BigDecimal.ZERO;
            int nombreServicesPayes = 0;
            
            for (Candidature candidature : candidaturesPaye) {
                List<TransactionPortefeuille> transactions = transactionRepository.findByCandidatureId(candidature.getIdCandidature());
                
                for (TransactionPortefeuille transaction : transactions) {
                    if (transaction.getTypeTransaction() == TransactionPortefeuille.TypeTransaction.CREDIT_MISSION &&
                        transaction.getStatutTransaction() == TransactionPortefeuille.StatutTransaction.EFFECTUE) {
                        
                        BigDecimal montantTotal = transaction.getMontant().add(
                            transaction.getCommissionEcodeli() != null ? transaction.getCommissionEcodeli() : BigDecimal.ZERO
                        );
                        
                        totalServices = totalServices.add(montantTotal);
                        nombreServicesPayes++;
                        break;
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalServicesPayes", totalServices);
            result.put("nombreServicesPayes", nombreServicesPayes);
            result.put("clientId", clientId);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Erreur calcul total dépensé client: " + e.getMessage());
            throw new RuntimeException("Erreur lors du calcul du total dépensé: " + e.getMessage());
        }
    }


    private PortefeuillePrestataire getOrCreatePortefeuille(Integer prestataireId) {
        Optional<PortefeuillePrestataire> existant = portefeuilleRepository.findByPrestataireIdUtilisateur(prestataireId);
        
        if (existant.isPresent()) {
            return existant.get();
        }
        
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(prestataireId);
        if (!utilisateurOpt.isPresent()) {
            throw new RuntimeException("Prestataire non trouvé avec ID: " + prestataireId);
        }
        
        Prestataire prestataire = (Prestataire) utilisateurOpt.get();
        
        PortefeuillePrestataire nouveauPortefeuille = new PortefeuillePrestataire();
        nouveauPortefeuille.setPrestataire(prestataire);
        nouveauPortefeuille.setIbanPrestataire("FR76 1234 5678 9012 3456 " + String.format("%03d", prestataireId));
        nouveauPortefeuille.setNomTitulaire(prestataire.getPrenom() + " " + prestataire.getNom());
        
        return portefeuilleRepository.save(nouveauPortefeuille);
    }


    private Map<String, Object> formatTransactionForResponse(TransactionPortefeuille transaction) {
        Map<String, Object> transactionMap = new HashMap<>();
        
        transactionMap.put("idTransaction", transaction.getIdTransaction());
        transactionMap.put("type", transaction.getTypeTransaction().name());
        transactionMap.put("typeLabel", transaction.getTypeTransaction().getLabel());
        transactionMap.put("montant", transaction.getMontant());
        transactionMap.put("statut", transaction.getStatutTransaction().name());
        transactionMap.put("statutLabel", transaction.getStatutTransaction().getLabel());
        transactionMap.put("description", transaction.getDescriptionComplete());
        transactionMap.put("dateTransaction", transaction.getDateTransaction());
        transactionMap.put("dateTraitement", transaction.getDateTraitement());
        transactionMap.put("reference", transaction.getReferenceExterne());
        transactionMap.put("isCredit", transaction.isCredit());
        transactionMap.put("isDebit", transaction.isDebit());
        
        if (transaction.getCandidature() != null && transaction.getCandidature().getDemandeService() != null) {
            transactionMap.put("missionTitre", transaction.getCandidature().getDemandeService().getTitre());
        }
        
        if (transaction.getIbanDestinataire() != null) {
            transactionMap.put("ibanDestination", masquerIban(transaction.getIbanDestinataire()));
        }
        
        return transactionMap;
    }


    private String masquerIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        return iban.substring(0, 4) + " ****" + iban.substring(iban.length() - 4);
    }


    private void envoyerEmailCreditMission(Prestataire prestataire, BigDecimal montant, String descriptionMission) {
        try {
            String subject = "💰 Mission payée - +" + montant + "€";
            String body = String.format(
                "<h2>Mission payée !</h2>" +
                "<p>Bonjour %s,</p>" +
                "<p>Excellente nouvelle ! Le client a validé votre mission <strong>\"%s\"</strong> et le paiement a été effectué.</p>" +
                "<div style=\"background: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                "<h3 style=\"margin: 0 0 10px 0; color: #1e40af;\">💰 +%s€ crédités sur votre portefeuille</h3>" +
                "<p style=\"margin: 0; color: #374151;\">Commission EcoDeli déduite (5%%)</p>" +
                "</div>" +
                "<p>Vous pouvez consulter votre portefeuille et demander un retrait à tout moment.</p>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/prestataire/portefeuille\" " +
                "style=\"background: #10b981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px;\">Voir mon portefeuille</a>" +
                "</div>" +
                "<p>Continuez votre excellent travail !</p>" +
                "<p>L'équipe EcoDeli</p>",
                prestataire.getPrenom(),
                descriptionMission,
                montant
            );
            
            emailService.sendHtmlEmail(prestataire.getEmail(), subject, body);
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email crédit mission: " + e.getMessage());
        }
    }


    private void envoyerEmailRetraitEffectue(Prestataire prestataire, BigDecimal montant, String iban) {
        try {
            String subject = "✅ Retrait effectué - " + montant + "€";
            String body = String.format(
                "<h2>Retrait effectué avec succès !</h2>" +
                "<p>Bonjour %s,</p>" +
                "<p>Votre demande de retrait a été traitée avec succès.</p>" +
                "<div style=\"background: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                "<h3 style=\"margin: 0 0 10px 0; color: #1e40af;\">💸 %s€ en cours de virement</h3>" +
                "<p style=\"margin: 5px 0; color: #374151;\"><strong>Destination :</strong> %s</p>" +
                "<p style=\"margin: 5px 0; color: #374151;\"><strong>Délai :</strong> 24-48h ouvrées</p>" +
                "</div>" +
                "<p>Les fonds seront crédités sur votre compte bancaire dans les prochaines 48h ouvrées.</p>" +
                "<div style=\"text-align: center; margin: 20px 0;\">" +
                "<a href=\"http://localhost:5173/prestataire/portefeuille\" " +
                "style=\"background: #10b981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px;\">Voir l'historique</a>" +
                "</div>" +
                "<p>Merci de votre confiance !</p>" +
                "<p>L'équipe EcoDeli</p>",
                prestataire.getPrenom(),
                montant,
                masquerIban(iban)
            );
            
            emailService.sendHtmlEmail(prestataire.getEmail(), subject, body);
            
        } catch (Exception e) {
            System.err.println("Erreur envoi email retrait: " + e.getMessage());
        }
    }
}