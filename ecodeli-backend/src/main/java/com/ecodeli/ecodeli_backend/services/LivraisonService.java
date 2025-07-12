package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.LivreurRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class LivraisonService {

    @Autowired
    private LivraisonRepository livraisonRepository;

    @Autowired
    private LivreurRepository livreurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PortefeuilleService portefeuilleService;

    private final Random random = new Random();

    public List<Livraison> getLivraisonsByLivreur(Integer livreurId) {
        return livraisonRepository.findAll().stream()
            .filter(livraison -> {
                if (livraison.getTypeLivraison() == Livraison.TypeLivraison.DIRECTE) {
                    return livraison.getAnnonce() != null &&
                           livraison.getAnnonce().getLivreur() != null &&
                           livraison.getAnnonce().getLivreur().getIdUtilisateur().equals(livreurId);
                }

                if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
                    return (livraison.getLivreurSegment1() != null &&
                            livraison.getLivreurSegment1().getIdUtilisateur().equals(livreurId)) ||
                           (livraison.getLivreurSegment2() != null &&
                            livraison.getLivreurSegment2().getIdUtilisateur().equals(livreurId));
                }

                return false;
            })
            .toList();
    }

    public void startLivraison(Integer livraisonId, Integer livreurId) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (!canStartLivraison(livraison, livreurId)) {
            throw new RuntimeException("Vous ne pouvez pas démarrer cette livraison pour le moment");
        }

        if (livraison.getTypeLivraison() == Livraison.TypeLivraison.DIRECTE) {
            if (livraison.getStatut() != Livraison.StatutLivraison.VALIDEE) {
                throw new RuntimeException("Cette livraison ne peut pas être démarrée");
            }

            livraison.setStatut(Livraison.StatutLivraison.EN_COURS);
            livraison.setDateDebut(LocalDateTime.now());

        } else if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
            if (isLivreurSegment1(livraison, livreurId)) {
                if (livraison.getStatut() != Livraison.StatutLivraison.VALIDEE) {
                    throw new RuntimeException("Le segment 1 ne peut pas être démarré");
                }

                livraison.setStatut(Livraison.StatutLivraison.EN_COURS);
                livraison.setDateDebut(LocalDateTime.now());

            } else if (isLivreurSegment2(livraison, livreurId)) {
                if (livraison.getStatut() != Livraison.StatutLivraison.ATTENTE_SEGMENT_2) {
                    throw new RuntimeException("Le segment 2 ne peut pas encore être démarré");
                }

                livraison.setStatut(Livraison.StatutLivraison.SEGMENT_2_EN_COURS);
                livraison.setDateCollecteEntrepot(LocalDateTime.now());
            }
        }

        livraisonRepository.save(livraison);
    }

    public void completeLivraison(Integer livraisonId, Integer livreurId) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getTypeLivraison() == Livraison.TypeLivraison.DIRECTE) {
            if (livraison.getStatut() != Livraison.StatutLivraison.EN_COURS) {
                throw new RuntimeException("Cette livraison ne peut pas être terminée");
            }

            generateAndSendOTP(livraison);
            livraison.setStatut(Livraison.StatutLivraison.ARRIVED);

        } else if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {

            if (isLivreurSegment1(livraison, livreurId)) {
                if (livraison.getStatut() != Livraison.StatutLivraison.EN_COURS) {
                    throw new RuntimeException("Le segment 1 ne peut pas être terminé");
                }

                livraison.setStatut(Livraison.StatutLivraison.ATTENTE_SEGMENT_2);
                livraison.setDateDepotEntrepot(LocalDateTime.now());

                if (livraison.getLivreurSegment2() != null) {
                    emailService.sendSegment2Notification(
                        getLivreurEmail(livraison.getLivreurSegment2()),
                        livraison.getAnnonce().getTitre(),
                        livraison.getEntrepotVille()
                    );
                }

            } else if (isLivreurSegment2(livraison, livreurId)) {
                if (livraison.getStatut() != Livraison.StatutLivraison.SEGMENT_2_EN_COURS) {
                    throw new RuntimeException("Le segment 2 ne peut pas être terminé");
                }

                generateAndSendOTP(livraison);
                livraison.setStatut(Livraison.StatutLivraison.ARRIVED);
            }
        }

        livraisonRepository.save(livraison);
    }

    public boolean validateOTP(Integer livraisonId, String otp) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatut() != Livraison.StatutLivraison.ARRIVED) {
            throw new RuntimeException("Cette livraison n'est pas en attente de validation OTP");
        }

        if (livraison.getOtpCode() == null ||
            !livraison.getOtpCode().equals(otp) ||
            livraison.getOtpTimestamp() == null ||
            LocalDateTime.now().isAfter(livraison.getOtpTimestamp().plusMinutes(5))) {
            return false;
        }

        livraison.setStatut(Livraison.StatutLivraison.TERMINEE);
        livraison.setDateFin(LocalDateTime.now());
        livraison.setValidation(true);
        livraison.setOtpCode(null);
        livraison.setOtpTimestamp(null);

        livraisonRepository.save(livraison);

        if (livraison.getAnnonce() != null) {
            Annonce annonce = livraison.getAnnonce();
            annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
            annonceRepository.save(annonce);

            crediterLivreurs(livraison, annonce);
        }

        return true;
    }

    private boolean canStartLivraison(Livraison livraison, Integer livreurId) {
        if (livraison.getTypeLivraison() == Livraison.TypeLivraison.DIRECTE) {
            return livraison.getAnnonce() != null &&
                   livraison.getAnnonce().getLivreur() != null &&
                   livraison.getAnnonce().getLivreur().getIdUtilisateur().equals(livreurId);
        }

        if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
            if (isLivreurSegment1(livraison, livreurId)) {
                return livraison.getStatut() == Livraison.StatutLivraison.VALIDEE;
            }

            if (isLivreurSegment2(livraison, livreurId)) {
                return livraison.getStatut() == Livraison.StatutLivraison.ATTENTE_SEGMENT_2;
            }
        }

        return false;
    }

    private boolean isLivreurSegment1(Livraison livraison, Integer livreurId) {
        return livraison.getLivreurSegment1() != null &&
               livraison.getLivreurSegment1().getIdUtilisateur().equals(livreurId);
    }

    private boolean isLivreurSegment2(Livraison livraison, Integer livreurId) {
        return livraison.getLivreurSegment2() != null &&
               livraison.getLivreurSegment2().getIdUtilisateur().equals(livreurId);
    }

    private void generateAndSendOTP(Livraison livraison) {
        String otp = String.format("%06d", random.nextInt(1000000));
        livraison.setOtpCode(otp);
        livraison.setOtpTimestamp(LocalDateTime.now());

        if (livraison.getAnnonce() != null &&
            livraison.getAnnonce().getEmailDestinataire() != null) {
            emailService.sendDeliveryOTP(
                livraison.getAnnonce().getEmailDestinataire(),
                otp,
                livraison.getAnnonce().getTitre()
            );
        }
    }

    private String getLivreurEmail(Livreur livreur) {
        return utilisateurRepository.findById(livreur.getIdUtilisateur())
            .map(utilisateur -> utilisateur.getEmail())
            .orElse("email.inconnu@ecodeli.com");
    }

    public Livraison getLivraisonById(Integer livraisonId) {
        return livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));
    }

    public String getSegmentForLivreur(Livraison livraison, Integer livreurId) {
        if (isLivreurSegment1(livraison, livreurId)) {
            return "SEGMENT_1";
        } else if (isLivreurSegment2(livraison, livreurId)) {
            return "SEGMENT_2";
        }
        return "NONE";
    }

    private void crediterLivreurs(Livraison livraison, Annonce annonce) {
        try {
            if (livraison.getTypeLivraison() == Livraison.TypeLivraison.DIRECTE) {
                if (annonce.getLivreur() != null) {
                    portefeuilleService.ajouterGains(
                        annonce.getLivreur().getIdUtilisateur(),
                        annonce.getPrixUnitaire()
                    );
                }

            } else if (livraison.getTypeLivraison() == Livraison.TypeLivraison.PARTIELLE) {
                if (livraison.getLivreurSegment1() != null && livraison.getLivreurSegment2() != null) {
                    java.math.BigDecimal montantParLivreur = annonce.getPrixUnitaire()
                        .divide(java.math.BigDecimal.valueOf(2));

                    portefeuilleService.ajouterGains(
                        livraison.getLivreurSegment1().getIdUtilisateur(),
                        montantParLivreur
                    );

                    portefeuilleService.ajouterGains(
                        livraison.getLivreurSegment2().getIdUtilisateur(),
                        montantParLivreur
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du crédit des gains: " + e.getMessage());
        }
    }

    public List<Livraison> getAllLivraisons() {
        return livraisonRepository.findAll();
    }

    public Livraison updateStatutLivraison(Integer livraisonId, Livraison.StatutLivraison nouveauStatut) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (!isValidStatusTransition(livraison.getStatut(), nouveauStatut)) {
            throw new RuntimeException("Transition de statut invalide de " +
                                     livraison.getStatut() + " vers " + nouveauStatut);
        }

        livraison.setStatut(nouveauStatut);

        switch (nouveauStatut) {
            case TERMINEE:
                if (livraison.getDateFin() == null) {
                    livraison.setDateFin(LocalDateTime.now());
                }
                livraison.setValidation(true);
                if (livraison.getAnnonce() != null) {
                    Annonce annonce = livraison.getAnnonce();
                    annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
                    annonceRepository.save(annonce);
                    crediterLivreurs(livraison, annonce);
                }
                break;

            case ANNULEE:
                livraison.setDateFin(LocalDateTime.now());
                livraison.setValidation(false);
                if (livraison.getAnnonce() != null) {
                    Annonce annonce = livraison.getAnnonce();
                    annonce.setStatut(Annonce.StatutAnnonce.PUBLIEE);
                    annonceRepository.save(annonce);
                }
                break;

            case EN_COURS:
                if (livraison.getDateDebut() == null) {
                    livraison.setDateDebut(LocalDateTime.now());
                }
                break;
        }

        return livraisonRepository.save(livraison);
    }

    public Livraison annulerLivraison(Integer livraisonId) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
            .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée"));

        if (livraison.getStatut() == Livraison.StatutLivraison.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une livraison terminée");
        }

        return updateStatutLivraison(livraisonId, Livraison.StatutLivraison.ANNULEE);
    }

    private boolean isValidStatusTransition(Livraison.StatutLivraison currentStatus,
                                          Livraison.StatutLivraison newStatus) {
        if (newStatus == Livraison.StatutLivraison.ANNULEE) {
            return currentStatus != Livraison.StatutLivraison.TERMINEE;
        }

        return switch (currentStatus) {
            case VALIDEE -> newStatus == Livraison.StatutLivraison.EN_COURS ||
                          newStatus == Livraison.StatutLivraison.TERMINEE;

            case EN_COURS -> newStatus == Livraison.StatutLivraison.ATTENTE_SEGMENT_2 ||
                            newStatus == Livraison.StatutLivraison.ARRIVED ||
                            newStatus == Livraison.StatutLivraison.TERMINEE;

            case ATTENTE_SEGMENT_2 -> newStatus == Livraison.StatutLivraison.SEGMENT_2_EN_COURS;

            case SEGMENT_2_EN_COURS -> newStatus == Livraison.StatutLivraison.ARRIVED ||
                                      newStatus == Livraison.StatutLivraison.TERMINEE;

            case ARRIVED -> newStatus == Livraison.StatutLivraison.TERMINEE;

            case TERMINEE -> false;

            case ANNULEE -> newStatus == Livraison.StatutLivraison.VALIDEE;
        };
    }
}
