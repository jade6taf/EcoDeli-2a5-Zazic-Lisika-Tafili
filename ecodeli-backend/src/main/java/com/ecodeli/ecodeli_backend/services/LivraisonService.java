package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livraison.StatutLivraison;
import com.ecodeli.ecodeli_backend.models.Livraison.TypeLivraison;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final AnnonceRepository annonceRepository;
    private final JavaMailSender mailSender;
    private final EntrepotUtilService entrepotUtilService;

    public LivraisonService(LivraisonRepository livraisonRepository,
                              AnnonceRepository annonceRepository,
                              JavaMailSender mailSender,
                              EntrepotUtilService entrepotUtilService) {
        this.livraisonRepository = livraisonRepository;
        this.annonceRepository = annonceRepository;
        this.mailSender = mailSender;
        this.entrepotUtilService = entrepotUtilService;
    }

    public List<Livraison> getAllLivraisons() {
        return livraisonRepository.findAll();
    }

    public List<Livraison> getLivraisonsByLivreur(Integer idLivreur) {
        return livraisonRepository.findByAnnonce_Livreur_IdUtilisateur(idLivreur);
    }

    @Transactional
    public Livraison demarrerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() != StatutLivraison.VALIDEE) {
            throw new IllegalArgumentException("Seule une livraison validée peut être démarrée");
        }
        livraison.setStatut(StatutLivraison.EN_COURS);
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.EN_COURS);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison arriverALivraison(Integer idLivraison) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + idLivraison));

        if (livraison.getStatut() != StatutLivraison.EN_COURS) {
            throw new IllegalStateException("La livraison doit être en cours pour marquer l'arrivée. Statut actuel: " + livraison.getStatut());
        }

        // Générer OTP
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(otpValue);

        livraison.setOtpCode(otpCode);
        livraison.setOtpTimestamp(LocalDateTime.now());
        livraison.setStatut(StatutLivraison.ARRIVED);

        // Envoyer l'OTP par e-mail au destinataire
        if (livraison.getDestinataire() != null && livraison.getDestinataire().getEmail() != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(livraison.getDestinataire().getEmail());
                message.setSubject("Votre code de confirmation EcoDeli pour la livraison N°" + livraison.getIdLivraison());
                message.setText("Bonjour " + livraison.getDestinataire().getPrenom() + ",\n\n" +
                                "Votre livreur est arrivé. Veuillez fournir le code suivant pour confirmer la réception de votre colis : " + otpCode + "\n\n" +
                                "Ce code est valide pendant 15 minutes.\n\n" +
                                "L'équipe EcoDeli");
                mailSender.send(message);
            } catch (Exception e) {
                // Erreur silencieuse pour l'envoi d'email - la livraison continue
            }
        } else {
            throw new IllegalStateException("Email du destinataire non configuré pour la livraison " + idLivraison + ". Impossible d'envoyer l'OTP.");
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison confirmerLivraisonParOtp(Integer idLivraison, String otpSoumis) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + idLivraison));

        if (livraison.getStatut() != StatutLivraison.ARRIVED) {
            throw new IllegalStateException("La livraison doit être au statut ARRIVED pour confirmer avec OTP. Statut actuel: " + livraison.getStatut());
        }

        if (livraison.getOtpCode() == null || !livraison.getOtpCode().equals(otpSoumis)) {
            throw new IllegalArgumentException("Code OTP invalide.");
        }

        if (livraison.getOtpTimestamp() == null || livraison.getOtpTimestamp().plusMinutes(15).isBefore(LocalDateTime.now())) {
            livraison.setOtpCode(null);
            livraison.setOtpTimestamp(null);
            livraisonRepository.save(livraison);
            throw new IllegalArgumentException("Code OTP expiré.");
        }
        livraison.setStatut(StatutLivraison.TERMINEE);
        livraison.setOtpCode(null);
        livraison.setOtpTimestamp(null);
        livraison.setDateFin(LocalDateTime.now());

        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison terminerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() != StatutLivraison.EN_COURS && livraison.getStatut() != StatutLivraison.ARRIVED) {
            throw new IllegalArgumentException("La livraison ne peut être terminée que si elle est EN_COURS ou ARRIVED (pour admin/fallback). Statut actuel: " + livraison.getStatut());
        }
        livraison.setStatut(StatutLivraison.TERMINEE);
        livraison.setDateFin(LocalDateTime.now());
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison annulerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() == StatutLivraison.TERMINEE) {
            throw new IllegalArgumentException("Une livraison terminée ne peut pas être annulée");
        }
        livraison.setStatut(StatutLivraison.ANNULEE);
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.ANNULEE);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    public List<Livraison> getLivraisonsEnAttenteSegment2() {
        return livraisonRepository.findByStatut(StatutLivraison.ATTENTE_SEGMENT_2);
    }

    public List<Livraison> getLivraisonsEnAttenteSegment2ParVille(String ville) {
        return livraisonRepository.findByStatutAndEntrepotVille(StatutLivraison.ATTENTE_SEGMENT_2, ville);
    }

    @Transactional
    public Livraison terminerSegment1(Integer idLivraison) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + idLivraison));

        if (livraison.getStatut() != StatutLivraison.EN_COURS || livraison.getTypeLivraison() != TypeLivraison.PARTIELLE) {
            throw new IllegalStateException("Cette méthode ne peut être utilisée que pour un segment 1 en cours de livraison partielle");
        }

        livraison.setStatut(StatutLivraison.ATTENTE_SEGMENT_2);
        livraison.setDateDepotEntrepot(LocalDateTime.now());

        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison demarrerSegment2(Integer idLivraison, Integer idLivreurSegment2) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + idLivraison));

        if (livraison.getStatut() != StatutLivraison.ATTENTE_SEGMENT_2) {
            throw new IllegalStateException("La livraison doit être en attente du segment 2. Statut actuel: " + livraison.getStatut());
        }

        // Récupérer le livreur segment 2 (on pourrait ajouter une validation ici)
        // Pour l'instant, on utilise juste l'ID fourni
        livraison.setStatut(StatutLivraison.SEGMENT_2_EN_COURS);
        livraison.setDateCollecteEntrepot(LocalDateTime.now());

        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison arriverSegment2(Integer idLivraison) {
        Livraison livraison = livraisonRepository.findById(idLivraison)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + idLivraison));

        if (livraison.getStatut() != StatutLivraison.SEGMENT_2_EN_COURS) {
            throw new IllegalStateException("Le segment 2 doit être en cours pour marquer l'arrivée. Statut actuel: " + livraison.getStatut());
        }

        // Générer OTP comme pour une livraison normale
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(otpValue);

        livraison.setOtpCode(otpCode);
        livraison.setOtpTimestamp(LocalDateTime.now());
        livraison.setStatut(StatutLivraison.ARRIVED);

        // Envoyer l'OTP par e-mail au destinataire
        if (livraison.getDestinataire() != null && livraison.getDestinataire().getEmail() != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(livraison.getDestinataire().getEmail());
                message.setSubject("Votre code de confirmation EcoDeli pour la livraison N°" + livraison.getIdLivraison());
                message.setText("Bonjour " + livraison.getDestinataire().getPrenom() + ",\n\n" +
                                "Votre livreur est arrivé pour la livraison finale. Veuillez fournir le code suivant pour confirmer la réception de votre colis : " + otpCode + "\n\n" +
                                "Ce code est valide pendant 15 minutes.\n\n" +
                                "L'équipe EcoDeli");
                mailSender.send(message);
            } catch (Exception e) {
                // Erreur silencieuse pour l'envoi d'email - la livraison continue
            }
        }
        return livraisonRepository.save(livraison);
    }
}
