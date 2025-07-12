package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.CandidatureLivraison;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.CandidatureLivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.LivreurRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.dto.request.CandidaturePartielleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidatureLivraisonService {

    @Autowired
    private CandidatureLivraisonRepository candidatureLivraisonRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LivreurRepository livreurRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LivraisonRepository livraisonRepository;

    public CandidatureLivraison postuler(Map<String, Object> candidatureData) {
        Integer annonceId = (Integer) candidatureData.get("annonceId");
        Integer livreurId = (Integer) candidatureData.get("livreurId");
        String message = (String) candidatureData.get("message");

        Annonce annonce = annonceRepository.findById(annonceId)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

        if (annonce.getStatut() != Annonce.StatutAnnonce.PUBLIEE) {
            throw new RuntimeException("Cette annonce n'est plus disponible");
        }

        Utilisateur livreur = utilisateurRepository.findById(livreurId)
            .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        if (candidatureLivraisonRepository.existsByAnnonceIdAnnonceAndLivreurIdUtilisateur(annonceId, livreurId)) {
            throw new RuntimeException("Vous avez déjà postulé pour cette annonce");
        }

        CandidatureLivraison candidature = new CandidatureLivraison();
        candidature.setAnnonce(annonce);
        candidature.setLivreur(livreur);
        candidature.setMessageLivreur(message);
        candidature.setStatut(CandidatureLivraison.StatutCandidatureLivraison.EN_ATTENTE);

        CandidatureLivraison savedCandidature = candidatureLivraisonRepository.save(candidature);

        String livreurNom = livreur.getPrenom() + " " + livreur.getNom();
        emailService.sendCandidatureNotification(
            annonce.getExpediteur().getEmail(),
            livreurNom,
            annonce.getTitre()
        );

        return savedCandidature;
    }

    public List<CandidatureLivraison> getCandidaturesByAnnonce(Integer annonceId) {
        return candidatureLivraisonRepository.findByAnnonceIdAnnonce(annonceId);
    }

    public List<CandidatureLivraison> getCandidaturesByLivreur(Integer livreurId) {
        return candidatureLivraisonRepository.findByLivreurIdUtilisateur(livreurId);
    }

    public void accepterCandidature(Long candidatureId, String commentaire) {
        CandidatureLivraison candidature = candidatureLivraisonRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (!candidature.peutEtreAcceptee()) {
            throw new RuntimeException("Cette candidature ne peut pas être acceptée");
        }

        candidature.accepter(commentaire);
        candidatureLivraisonRepository.save(candidature);

        Annonce annonce = candidature.getAnnonce();
        Utilisateur utilisateurLivreur = candidature.getLivreur();

        Livreur livreur = livreurRepository.findById(utilisateurLivreur.getIdUtilisateur())
            .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec l'ID: " + utilisateurLivreur.getIdUtilisateur()));

        annonce.setLivreur(livreur);
        annonce.setStatut(Annonce.StatutAnnonce.VALIDEE);
        annonceRepository.save(annonce);

        Livraison livraison = new Livraison();
        livraison.setAnnonce(annonce);
        livraison.setExpediteur(annonce.getExpediteur());
        livraison.setAdresseEnvoi(annonce.getAdresseDepart());
        livraison.setAdresseDeLivraison(annonce.getAdresseFin());
        livraison.setPrix(annonce.getPrixUnitaire().intValue());
        livraison.setTypeLivraison(Livraison.TypeLivraison.DIRECTE);
        livraison.setStatut(Livraison.StatutLivraison.VALIDEE);

        livraisonRepository.save(livraison);

        List<CandidatureLivraison> autresCandidatures = candidatureLivraisonRepository
            .findByAnnonceIdAnnonce(annonce.getIdAnnonce());

        for (CandidatureLivraison autre : autresCandidatures) {
            if (!autre.getIdCandidatureLivraison().equals(candidatureId) &&
                autre.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.EN_ATTENTE) {
                autre.refuser("Une autre candidature a été acceptée");
                candidatureLivraisonRepository.save(autre);
            }
        }
    }

    public void refuserCandidature(Long candidatureId, String commentaire) {
        CandidatureLivraison candidature = candidatureLivraisonRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (!candidature.peutEtreRefusee()) {
            throw new RuntimeException("Cette candidature ne peut pas être refusée");
        }

        candidature.refuser(commentaire);
        candidatureLivraisonRepository.save(candidature);
    }

    public CandidatureLivraison getCandidatureById(Long candidatureId) {
        return candidatureLivraisonRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));
    }

    public CandidatureLivraison candidaterPartielle(CandidaturePartielleRequest request) {
        Annonce annonce = annonceRepository.findById(request.getAnnonceId())
            .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée"));

        if (!Boolean.TRUE.equals(annonce.getLivraisonPartielleAutorisee())) {
            throw new RuntimeException("Cette annonce n'autorise pas les livraisons partielles");
        }

        if (annonce.getStatut() != Annonce.StatutAnnonce.PUBLIEE) {
            throw new RuntimeException("Cette annonce n'est plus disponible");
        }

        Utilisateur livreur = utilisateurRepository.findById(request.getLivreurId())
            .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        boolean dejaCandidate = candidatureLivraisonRepository.existsByAnnonceIdAnnonceAndLivreurIdUtilisateur(
            request.getAnnonceId(), request.getLivreurId());

        if (dejaCandidate) {
            throw new RuntimeException("Vous avez déjà candidaté pour cette annonce. Un livreur ne peut candidater que pour un seul segment.");
        }

        List<CandidatureLivraison> candidaturesExistantes = candidatureLivraisonRepository
            .findByAnnonceIdAnnonce(request.getAnnonceId());

        boolean segmentDejaAccepte = candidaturesExistantes.stream()
            .anyMatch(c -> c.getSegmentLivraison() == request.getSegment() &&
                          c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE);

        if (segmentDejaAccepte) {
            throw new RuntimeException("Ce segment a déjà été accepté par un autre livreur");
        }

        CandidatureLivraison candidature = new CandidatureLivraison();
        candidature.setAnnonce(annonce);
        candidature.setLivreur(livreur);
        candidature.setMessageLivreur(request.getMessageLivreur());
        candidature.setStatut(CandidatureLivraison.StatutCandidatureLivraison.EN_ATTENTE);
        candidature.setSegmentLivraison(request.getSegment());
        candidature.setEntrepotChoisi(request.getEntrepotChoisi());

        CandidatureLivraison savedCandidature = candidatureLivraisonRepository.save(candidature);

        String livreurNom = livreur.getPrenom() + " " + livreur.getNom();
        String segmentLabel = request.getSegment().getLibelle();
        emailService.sendCandidaturePartielleNotification(
            annonce.getExpediteur().getEmail(),
            livreurNom,
            annonce.getTitre(),
            segmentLabel,
            request.getEntrepotChoisi()
        );

        return savedCandidature;
    }

    public Map<String, Object> getCandidaturesParSegment(Integer annonceId) {
        List<CandidatureLivraison> candidatures = candidatureLivraisonRepository.findByAnnonceIdAnnonce(annonceId);

        Map<String, List<CandidatureLivraison>> candidaturesParSegment = candidatures.stream()
            .filter(c -> c.getSegmentLivraison() != null)
            .collect(Collectors.groupingBy(c -> c.getSegmentLivraison().name()));

        Map<String, Object> result = new HashMap<>();
        result.put("segment1", candidaturesParSegment.getOrDefault("SEGMENT_1", new ArrayList<>()));
        result.put("segment2", candidaturesParSegment.getOrDefault("SEGMENT_2", new ArrayList<>()));

        boolean segment1Accepte = candidatures.stream()
            .anyMatch(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_1 &&
                          c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE);

        boolean segment2Accepte = candidatures.stream()
            .anyMatch(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_2 &&
                          c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE);

        result.put("segment1Accepte", segment1Accepte);
        result.put("segment2Accepte", segment2Accepte);
        result.put("segmentsComplets", segment1Accepte && segment2Accepte);

        return result;
    }

    public void accepterCandidaturePartielle(Long candidatureId, String commentaire) {
        CandidatureLivraison candidature = candidatureLivraisonRepository.findById(candidatureId)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée"));

        if (!candidature.peutEtreAcceptee()) {
            throw new RuntimeException("Cette candidature ne peut pas être acceptée");
        }

        List<CandidatureLivraison> candidaturesAcceptees = candidatureLivraisonRepository
            .findByAnnonceIdAnnonce(candidature.getAnnonce().getIdAnnonce())
            .stream()
            .filter(c -> c.getSegmentLivraison() == candidature.getSegmentLivraison() &&
                        c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE)
            .toList();

        if (!candidaturesAcceptees.isEmpty()) {
            throw new RuntimeException("Une candidature est déjà acceptée pour ce segment");
        }

        candidature.accepter(commentaire);
        candidatureLivraisonRepository.save(candidature);

        List<CandidatureLivraison> candidaturesMemeSegment = candidatureLivraisonRepository
            .findByAnnonceIdAnnonce(candidature.getAnnonce().getIdAnnonce())
            .stream()
            .filter(c -> c.getSegmentLivraison() == candidature.getSegmentLivraison() &&
                        !c.getIdCandidatureLivraison().equals(candidatureId) &&
                        c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.EN_ATTENTE)
            .toList();

        for (CandidatureLivraison autre : candidaturesMemeSegment) {
            autre.refuser("Une autre candidature a été acceptée pour ce segment");
            candidatureLivraisonRepository.save(autre);
        }

        updateAnnonceStatutAfterAcceptation(candidature.getAnnonce());
    }

    private void updateAnnonceStatutAfterAcceptation(Annonce annonce) {
        List<CandidatureLivraison> candidatures = candidatureLivraisonRepository.findByAnnonceIdAnnonce(annonce.getIdAnnonce());

        boolean segment1Accepte = candidatures.stream()
            .anyMatch(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_1 &&
                          c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE);

        boolean segment2Accepte = candidatures.stream()
            .anyMatch(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_2 &&
                          c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE);

        if (segment1Accepte && segment2Accepte) {
            annonce.setStatut(Annonce.StatutAnnonce.SEGMENTS_COMPLETS);
        } else if (segment1Accepte) {
            annonce.setStatut(Annonce.StatutAnnonce.SEGMENT_1_PRIS);
        } else if (segment2Accepte) {
            annonce.setStatut(Annonce.StatutAnnonce.SEGMENT_2_PRIS);
        } else {
            annonce.setStatut(Annonce.StatutAnnonce.PUBLIEE);
        }

        annonceRepository.save(annonce);
    }
}
