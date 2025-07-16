package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.models.CandidatureLivraison;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.repositories.CandidatureLivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.LivreurRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AnnonceService {

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CandidatureLivraisonRepository candidatureLivraisonRepository;

    @Autowired
    private LivreurRepository livreurRepository;

    @Autowired
    private LivraisonRepository livraisonRepository;

    @Value("${GOOGLE_MAPS_API_KEY}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    private static final BigDecimal TARIF_PAR_KM = new BigDecimal("0.80");

    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    public Annonce getAnnonceById(Integer id) {
        return annonceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée avec l'ID: " + id));
    }

    public Annonce createAnnonce(Map<String, Object> annonceData) {
        try {
            Object expediteurIdObj = annonceData.get("expediteurId");
            System.out.println("expediteurId reçu: " + expediteurIdObj + " (type: " + (expediteurIdObj != null ? expediteurIdObj.getClass().getSimpleName() : "null") + ")");

            if (expediteurIdObj == null) {
                throw new RuntimeException("expediteurId est null dans les données reçues");
            }

            Integer expediteurId = (Integer) expediteurIdObj;
            Utilisateur expediteur = utilisateurRepository.findById(expediteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur non trouvé avec l'ID: " + expediteurId));

           Colis colis = new Colis();
            Map<String, Object> colisData = (Map<String, Object>) annonceData.get("colis");
            if (colisData != null) {
                if (colisData.get("poids") != null) {
                    colis.setPoids(new BigDecimal(colisData.get("poids").toString()));
                }
                if (colisData.get("longueur") != null) {
                    colis.setLongueur(new BigDecimal(colisData.get("longueur").toString()));
                }
                if (colisData.get("largeur") != null) {
                    colis.setLargeur(new BigDecimal(colisData.get("largeur").toString()));
                }
                if (colisData.get("hauteur") != null) {
                    colis.setHauteur(new BigDecimal(colisData.get("hauteur").toString()));
                }
                if (colisData.get("fragile") != null) {
                    colis.setFragile((Boolean) colisData.get("fragile"));
                }
                if (colisData.get("description") != null) {
                    colis.setDescription((String) colisData.get("description"));
                }
            }

            Annonce annonce = new Annonce();
            annonce.setTitre((String) annonceData.get("titre"));
            annonce.setDescription((String) annonceData.get("description"));
            annonce.setAdresseDepart((String) annonceData.get("adresseDepart"));
            annonce.setAdresseFin((String) annonceData.get("adresseFin"));
            annonce.setEmailDestinataire((String) annonceData.get("emailDestinataire"));
            annonce.setExpediteur(expediteur);
            annonce.setColis(colis);

            Boolean livraisonPartielleAutorisee = (Boolean) annonceData.get("livraisonPartielleAutorisee");
            if (livraisonPartielleAutorisee != null) {
                annonce.setLivraisonPartielleAutorisee(livraisonPartielleAutorisee);
            }

            BigDecimal distance = calculateDistance(annonce.getAdresseDepart(), annonce.getAdresseFin());
            BigDecimal prix = distance.multiply(TARIF_PAR_KM).setScale(2, RoundingMode.HALF_UP);
            annonce.setPrixUnitaire(prix);

            annonce.setStatut(Annonce.StatutAnnonce.PUBLIEE);
            annonce.setTypeAnnonce(Annonce.TypeAnnonce.unique);

            return annonceRepository.save(annonce);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'annonce: " + e.getMessage());
        }
    }

    public BigDecimal calculateDistance(String adresseDepart, String adresseFin) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&units=metric&key=%s",
                adresseDepart.replace(" ", "+"),
                adresseFin.replace(" ", "+"),
                googleMapsApiKey
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
                if (!rows.isEmpty()) {
                    List<Map<String, Object>> elements = (List<Map<String, Object>>) rows.get(0).get("elements");
                    if (!elements.isEmpty()) {
                        Map<String, Object> element = elements.get(0);
                        if ("OK".equals(element.get("status"))) {
                            Map<String, Object> distance = (Map<String, Object>) element.get("distance");
                            Integer distanceInMeters = (Integer) distance.get("value");

                            return new BigDecimal(distanceInMeters).divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                        }
                    }
                }
            }
            return new BigDecimal("10.00");
        } catch (Exception e) {
            return new BigDecimal("10.00");
        }
    }

    public List<Annonce> getAnnoncesByUser(Integer userId) {
        return annonceRepository.findAll().stream()
            .filter(annonce -> annonce.getExpediteur() != null &&
                     annonce.getExpediteur().getIdUtilisateur().equals(userId))
            .toList();
    }

    public void cancelAnnonce(Integer annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatut() == Annonce.StatutAnnonce.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une annonce terminée");
        }

        if (annonce.getStatut() == Annonce.StatutAnnonce.ANNULEE) {
            throw new RuntimeException("Cette annonce est déjà annulée");
        }

        annonce.setStatut(Annonce.StatutAnnonce.ANNULEE);
        annonceRepository.save(annonce);
    }

    public List<Annonce> getAvailableAnnonces() {
        return annonceRepository.findAll().stream()
            .filter(annonce -> annonce.getStatut() == Annonce.StatutAnnonce.PUBLIEE)
            .toList();
    }

    public void takeAnnonce(Integer annonceId, Integer livreurId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatut() != Annonce.StatutAnnonce.PUBLIEE) {
            throw new RuntimeException("Cette annonce n'est plus disponible");
        }

        Livreur livreur = livreurRepository.findById(livreurId)
            .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé"));

        annonce.setLivreur(livreur);
        annonce.setStatut(Annonce.StatutAnnonce.VALIDEE);

        Livraison livraison = new Livraison();
        livraison.setAnnonce(annonce);
        livraison.setExpediteur(annonce.getExpediteur());
        livraison.setAdresseEnvoi(annonce.getAdresseDepart());
        livraison.setAdresseDeLivraison(annonce.getAdresseFin());
        livraison.setPrix(annonce.getPrixUnitaire().intValue());

        livraison.setTypeLivraison(Livraison.TypeLivraison.DIRECTE);
        livraison.setStatut(Livraison.StatutLivraison.VALIDEE);

        livraisonRepository.save(livraison);
        annonceRepository.save(annonce);
    }

    public void startDelivery(Integer annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatut() != Annonce.StatutAnnonce.VALIDEE) {
            throw new RuntimeException("La livraison ne peut pas être commencée pour cette annonce");
        }

        annonce.setStatut(Annonce.StatutAnnonce.EN_COURS);
        annonceRepository.save(annonce);
    }

    public void completeDelivery(Integer annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatut() != Annonce.StatutAnnonce.EN_COURS) {
            throw new RuntimeException("La livraison ne peut pas être terminée pour cette annonce");
        }

        annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
        annonceRepository.save(annonce);
    }

    public List<Annonce> getAnnoncesByLivreur(Integer livreurId) {
        return annonceRepository.findAll().stream()
            .filter(annonce -> annonce.getLivreur() != null &&
                     annonce.getLivreur().getIdUtilisateur().equals(livreurId))
            .toList();
    }

    public String generateDeliveryCode(Integer annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatut() != Annonce.StatutAnnonce.EN_COURS) {
            throw new RuntimeException("Impossible de générer un code pour cette livraison");
        }

        if (annonce.getEmailDestinataire() == null || annonce.getEmailDestinataire().isEmpty()) {
            throw new RuntimeException("Aucun email destinataire défini pour cette livraison");
        }

        String code = String.format("%06d", random.nextInt(1000000));

        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        annonce.setCodeValidation(code);
        annonce.setCodeExpiration(expiration);
        annonceRepository.save(annonce);

        emailService.sendDeliveryCode(annonce.getEmailDestinataire(), code, annonce.getTitre());

        return code;
    }

    public boolean validateDeliveryCode(Integer annonceId, String code) {
        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getCodeValidation() == null) {
            throw new RuntimeException("Aucun code de validation généré pour cette livraison");
        }

        if (annonce.getCodeExpiration() == null || LocalDateTime.now().isAfter(annonce.getCodeExpiration())) {
            annonce.setCodeValidation(null);
            annonce.setCodeExpiration(null);
            annonceRepository.save(annonce);
            throw new RuntimeException("Le code de validation a expiré");
        }

        if (!annonce.getCodeValidation().equals(code)) {
            return false;
        }

        annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
        annonce.setCodeValidation(null);
        annonce.setCodeExpiration(null);
        annonceRepository.save(annonce);

        return true;
    }

    public BigDecimal calculatePrice(BigDecimal distance) {
        return distance.multiply(TARIF_PAR_KM).setScale(2, RoundingMode.HALF_UP);
    }

    public void validerLivreursPartielle(Integer annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        if (!Boolean.TRUE.equals(annonce.getLivraisonPartielleAutorisee())) {
            throw new RuntimeException("Cette annonce n'autorise pas les livraisons partielles");
        }

        if (annonce.getStatut() != Annonce.StatutAnnonce.SEGMENTS_COMPLETS) {
            throw new RuntimeException("Les deux segments ne sont pas encore pris en charge");
        }

        List<CandidatureLivraison> candidatures = candidatureLivraisonRepository.findByAnnonceIdAnnonce(annonceId);

        CandidatureLivraison candidatureSegment1 = candidatures.stream()
            .filter(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_1 &&
                        c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Aucune candidature acceptée pour le segment 1"));

        CandidatureLivraison candidatureSegment2 = candidatures.stream()
            .filter(c -> c.getSegmentLivraison() == CandidatureLivraison.SegmentLivraison.SEGMENT_2 &&
                        c.getStatut() == CandidatureLivraison.StatutCandidatureLivraison.ACCEPTEE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Aucune candidature acceptée pour le segment 2"));

        Integer livreur1Id = candidatureSegment1.getLivreur().getIdUtilisateur();
        Integer livreur2Id = candidatureSegment2.getLivreur().getIdUtilisateur();

        Livreur livreur1 = livreurRepository.findById(livreur1Id)
            .orElseThrow(() -> new ResourceNotFoundException("Livreur 1 non trouvé"));

        Livreur livreur2 = livreurRepository.findById(livreur2Id)
            .orElseThrow(() -> new ResourceNotFoundException("Livreur 2 non trouvé"));

        annonce.setLivreurSegment1(livreur1);
        annonce.setLivreurSegment2(livreur2);
        annonce.setEntrepotIntermediaire(candidatureSegment1.getEntrepotChoisi());

        Livraison livraison = new Livraison();
        livraison.setAnnonce(annonce);
        livraison.setExpediteur(annonce.getExpediteur());
        livraison.setAdresseEnvoi(annonce.getAdresseDepart());
        livraison.setAdresseDeLivraison(annonce.getAdresseFin());
        livraison.setPrix(annonce.getPrixUnitaire().intValue());

        livraison.setTypeLivraison(Livraison.TypeLivraison.PARTIELLE);
        livraison.setLivreurSegment1(livreur1);
        livraison.setLivreurSegment2(livreur2);
        livraison.setEntrepotVille(candidatureSegment1.getEntrepotChoisi());
        livraison.setStatut(Livraison.StatutLivraison.VALIDEE);

        livraisonRepository.save(livraison);

        annonce.setStatut(Annonce.StatutAnnonce.VALIDEE);
        annonceRepository.save(annonce);
    }
}
