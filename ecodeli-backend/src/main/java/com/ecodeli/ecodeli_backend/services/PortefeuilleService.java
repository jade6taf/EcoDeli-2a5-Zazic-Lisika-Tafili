package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Retrait;
import com.ecodeli.ecodeli_backend.repositories.LivreurRepository;
import com.ecodeli.ecodeli_backend.repositories.RetraitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortefeuilleService {

    private final LivreurRepository livreurRepository;
    private final RetraitRepository retraitRepository;

    private static final BigDecimal COMMISSION_ECODELI = BigDecimal.valueOf(0.10);

    @Transactional
    public void ajouterGains(Integer livreurId, BigDecimal montantTotal) {
        Livreur livreur = livreurRepository.findById(livreurId)
            .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        BigDecimal montantLivreur = montantTotal.multiply(BigDecimal.ONE.subtract(COMMISSION_ECODELI));

        livreur.setSoldePortefeuille(livreur.getSoldePortefeuille().add(montantLivreur));
        livreur.setTotalGagnes(livreur.getTotalGagnes().add(montantLivreur));

        livreurRepository.save(livreur);

        log.info("Gains ajoutés pour livreur {}: {}€ (commission EcoDeli: {}€)",
                livreurId, montantLivreur, montantTotal.multiply(COMMISSION_ECODELI));
    }

    @Transactional
    public boolean traiterRetrait(Integer livreurId, BigDecimal montant, String iban) {
        Livreur livreur = livreurRepository.findById(livreurId)
            .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));

        if (livreur.getSoldePortefeuille().compareTo(montant) < 0) {
            log.warn("Solde insuffisant pour livreur {}: demandé {}€, disponible {}€",
                    livreurId, montant, livreur.getSoldePortefeuille());
            return false;
        }

        Retrait retrait = new Retrait();
        retrait.setLivreur(livreur);
        retrait.setMontant(montant);
        retrait.setIban(iban);
        retrait.setStatut(Retrait.StatutRetrait.TRAITE);
        retrait.setDateTraitement(LocalDateTime.now());

        retraitRepository.save(retrait);

        livreur.setSoldePortefeuille(livreur.getSoldePortefeuille().subtract(montant));
        livreur.setIbanLivreur(iban);
        livreurRepository.save(livreur);

        log.info("Retrait traité pour livreur {}: {}€ vers IBAN {}",
                livreurId, montant, iban.substring(0, 8) + "****");
        return true;
    }

    public List<Retrait> getHistoriqueRetraits(Integer livreurId) {
        return retraitRepository.findByLivreurIdUtilisateurOrderByDateDemandeDesc(livreurId);
    }

    public Livreur getPortefeuille(Integer livreurId) {
        return livreurRepository.findById(livreurId)
            .orElseThrow(() -> new RuntimeException("Livreur non trouvé"));
    }
}
