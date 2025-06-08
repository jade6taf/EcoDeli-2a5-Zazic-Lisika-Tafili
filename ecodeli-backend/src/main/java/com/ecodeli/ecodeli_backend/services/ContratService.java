package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.ContratHistoryRepository;
import com.ecodeli.ecodeli_backend.repositories.ContratRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ContratService {

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private ContratHistoryRepository contratHistoryRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<Contrat> getAllContrats() {
        return contratRepository.findAll();
    }

    public Optional<Contrat> getContratById(Integer id) {
        return contratRepository.findById(id);
    }

    public List<Contrat> getContratsByStatut(StatutContrat statut) {
        return contratRepository.findByStatut(statut);
    }

    public List<Contrat> getContratsCommercant(Integer idCommercant) {
        return contratRepository.findByCommercantIdUtilisateur(idCommercant);
    }

    public Optional<Contrat> getContratCommercant(Integer idCommercant, Integer idContrat) {
        return contratRepository.findById(idContrat)
                .filter(contrat -> contrat.getCommercant().getIdUtilisateur().equals(idCommercant));
    }

    public Contrat signerContratCommercant(Integer idContrat, Integer idCommercant) {
        Optional<Contrat> optContrat = getContratCommercant(idCommercant, idContrat);

        if (optContrat.isEmpty()) {
            throw new IllegalArgumentException("Contrat non trouvé ou non autorisé");
        }

        Contrat contrat = optContrat.get();

        if (!contrat.peutEtreSigneParCommercant()) {
            throw new IllegalStateException("Ce contrat ne peut pas être signé dans son état actuel");
        }

        contrat.setDateSignatureCommercant(LocalDateTime.now());
        contrat.setStatut(StatutContrat.ATTENTE_SIGNATURE_ADMIN);

        ajouterHistorique(contrat, StatutContrat.ATTENTE_SIGNATURE_COMMERCANT,
                         StatutContrat.ATTENTE_SIGNATURE_ADMIN,
                         "Signature du commerçant", contrat.getCommercant());

        return contratRepository.save(contrat);
    }

    public Contrat refuserContratCommercant(Integer idContrat, Integer idCommercant, String motif) {
        Optional<Contrat> optContrat = getContratCommercant(idCommercant, idContrat);

        if (optContrat.isEmpty()) {
            throw new IllegalArgumentException("Contrat non trouvé ou non autorisé");
        }

        Contrat contrat = optContrat.get();

        if (!contrat.peutEtreSigneParCommercant()) {
            throw new IllegalStateException("Ce contrat ne peut pas être refusé dans son état actuel");
        }

        StatutContrat ancienStatut = contrat.getStatut();
        contrat.setStatut(StatutContrat.REFUSE);

        ajouterHistorique(contrat, ancienStatut, StatutContrat.REFUSE,
                         "Refus du commerçant : " + motif, contrat.getCommercant());

        return contratRepository.save(contrat);
    }

    public Contrat createContrat(Contrat contrat, Integer idAdmin) {

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        if (!(admin instanceof Admin)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un administrateur");
        }

        contrat.setNumeroContrat(genererNumeroContrat());
        contrat.setAdmin((Admin) admin);
        contrat.setStatut(StatutContrat.BROUILLON);

        Contrat nouveauContrat = contratRepository.save(contrat);

        ajouterHistorique(nouveauContrat, null, StatutContrat.BROUILLON,
                         "Création du contrat", admin);

        return nouveauContrat;
    }

    public Contrat updateContrat(Integer idContrat, Contrat contratDetails, Integer idAdmin) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (!contrat.peutEtreModifie()) {
            throw new IllegalStateException("Ce contrat ne peut plus être modifié");
        }

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        contrat.setCommissionPourcentage(contratDetails.getCommissionPourcentage());
        contrat.setFraisFixeMensuel(contratDetails.getFraisFixeMensuel());
        contrat.setConditionsService(contratDetails.getConditionsService());
        contrat.setDateDebut(contratDetails.getDateDebut());
        contrat.setDateFin(contratDetails.getDateFin());

        ajouterHistorique(contrat, StatutContrat.BROUILLON, StatutContrat.BROUILLON,
                         "Modification du contrat", admin);

        return contratRepository.save(contrat);
    }

    public Contrat envoyerPourSignature(Integer idContrat, Integer idAdmin) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (!contrat.peutEtreEnvoyePourSignature()) {
            throw new IllegalStateException("Ce contrat ne peut pas être envoyé pour signature");
        }

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        contrat.setStatut(StatutContrat.ATTENTE_SIGNATURE_COMMERCANT);

        ajouterHistorique(contrat, StatutContrat.BROUILLON,
                         StatutContrat.ATTENTE_SIGNATURE_COMMERCANT,
                         "Envoi pour signature au commerçant", admin);

        return contratRepository.save(contrat);
    }

    public Contrat validerContratAdmin(Integer idContrat, Integer idAdmin) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (!contrat.peutEtreValideParAdmin()) {
            throw new IllegalStateException("Ce contrat ne peut pas être validé dans son état actuel");
        }

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        contrat.setDateSignatureAdmin(LocalDateTime.now());
        contrat.setStatut(StatutContrat.ACTIF);

        ajouterHistorique(contrat, StatutContrat.ATTENTE_SIGNATURE_ADMIN,
                         StatutContrat.ACTIF,
                         "Validation et signature administrateur", admin);

        return contratRepository.save(contrat);
    }

    public Contrat refuserContratAdmin(Integer idContrat, Integer idAdmin, String motif) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (!contrat.peutEtreValideParAdmin()) {
            throw new IllegalStateException("Ce contrat ne peut pas être refusé dans son état actuel");
        }

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        contrat.setStatut(StatutContrat.REFUSE);

        ajouterHistorique(contrat, StatutContrat.ATTENTE_SIGNATURE_ADMIN,
                         StatutContrat.REFUSE,
                         "Refus administrateur : " + motif, admin);

        return contratRepository.save(contrat);
    }

    public void deleteContrat(Integer idContrat, Integer idAdmin) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat non trouvé"));

        if (!contrat.peutEtreModifie()) {
            throw new IllegalStateException("Ce contrat ne peut pas être supprimé");
        }

        Utilisateur admin = utilisateurRepository.findById(idAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));

        contratHistoryRepository.deleteAll(
            contratHistoryRepository.findByContratIdContratOrderByDateActionDesc(idContrat)
        );

        contratRepository.delete(contrat);
    }

    public Map<String, Object> getStatistiquesAdmin() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> statutStats = new HashMap<>();
        for (StatutContrat statut : StatutContrat.values()) {
            statutStats.put(statut.name(), contratRepository.countByStatut(statut));
        }
        stats.put("contratsParStatut", statutStats);

        stats.put("totalContrats", contratRepository.count());

        stats.put("contratsActifs", contratRepository.countByStatut(StatutContrat.ACTIF));

        long enAttente = contratRepository.countByStatut(StatutContrat.ATTENTE_SIGNATURE_COMMERCANT) +
                        contratRepository.countByStatut(StatutContrat.ATTENTE_SIGNATURE_ADMIN);
        stats.put("contratsEnAttente", enAttente);

        LocalDateTime dateLimit = LocalDateTime.now().minusDays(30);
        stats.put("contratsRecents", contratRepository.findContratsRecents(dateLimit));

        stats.put("dernieresActions", contratHistoryRepository.findTop10ByOrderByDateActionDesc());

        return stats;
    }

    public Map<String, Object> getStatistiquesCommercant(Integer idCommercant) {
        Map<String, Object> stats = new HashMap<>();

        List<Contrat> contrats = getContratsCommercant(idCommercant);
        stats.put("totalContrats", contrats.size());

        long contratsActifs = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.ACTIF)
                .count();
        stats.put("contratActif", contratsActifs > 0);

        long enAttenteSignature = contrats.stream()
                .filter(c -> c.getStatut() == StatutContrat.ATTENTE_SIGNATURE_COMMERCANT)
                .count();
        stats.put("enAttenteSignature", enAttenteSignature);

        return stats;
    }

    private String genererNumeroContrat() {
        String base = "CT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int compteur = 1;
        String numero;

        do {
            numero = base + String.format("%03d", compteur);
            compteur++;
        } while (contratRepository.existsByNumeroContrat(numero));

        return numero;
    }

    private void ajouterHistorique(Contrat contrat, StatutContrat statutPrecedent,
                                  StatutContrat statutNouveau, String commentaire,
                                  Utilisateur utilisateur) {
        ContratHistory historique = new ContratHistory(contrat, statutPrecedent,
                                                      statutNouveau, commentaire, utilisateur);
        contratHistoryRepository.save(historique);
    }

    public List<ContratHistory> getHistoriqueContrat(Integer idContrat) {
        return contratHistoryRepository.findByContratIdContratOrderByDateActionDesc(idContrat);
    }
}
