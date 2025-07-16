package com.ecodeli.analytics.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Getter
@Setter
public class LivraisonDto {
    private Integer idLivraison;
    private String statut;
    private String statutLabel;
    private String typeLivraison;
    private String typeLivraisonLabel;
    private Integer prix;
    private Double poids;
    private String adresseEnvoi;
    private String codePostalEnvoi;
    private String adresseDeLivraison;
    private String codePostalLivraison;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLivraison;
    private Boolean validation;
    private String codeValidation;
    private String entrepotVille;

    private Map<String, Object> expediteur;
    private Map<String, Object> destinataire;
    private Map<String, Object> annonce;
    private Map<String, Object> livreurSegment1;
    private Map<String, Object> livreurSegment2;

    public Integer getId() {
        return idLivraison;
    }

    public void setId(Integer id) {
        this.idLivraison = id;
    }

    public String getType() {
        return typeLivraison;
    }

    public void setType(String type) {
        this.typeLivraison = type;
    }

    public String getAdresseDepart() {
        return adresseEnvoi;
    }

    public void setAdresseDepart(String adresse) {
        this.adresseEnvoi = adresse;
    }

    public String getAdresseArrivee() {
        return adresseDeLivraison;
    }

    public void setAdresseArrivee(String adresse) {
        this.adresseDeLivraison = adresse;
    }

    public Map<String, Object> getExpediteur() {
        return expediteur;
    }

    public Map<String, Object> getDestinataire() {
        return destinataire;
    }
}
