package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONTRAT_COMMERCANT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratCommercant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer idContrat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commercant", nullable = false)
    private Commercant commercant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutContrat statut = StatutContrat.DEMANDE_ENVOYEE;
    
    @Column(name = "contenu_contrat", columnDefinition = "TEXT")
    private String contenuContrat;
    
    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;
    
    @Column(name = "date_creation_contrat")
    private LocalDateTime dateCreationContrat;
    
    @Column(name = "date_signature")
    private LocalDateTime dateSignature;
    
    @Column(name = "commentaire_admin", columnDefinition = "TEXT")
    private String commentaireAdmin;
    
    @Column(name = "signature_commercant")
    private Boolean signatureCommercant = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_createur")
    private Admin adminCreateur;
    
    public enum StatutContrat {
        DEMANDE_ENVOYEE("Demande envoyée"),
        EN_ATTENTE_ADMIN("En attente admin"),
        CONTRAT_CREE("Contrat créé"),
        SIGNE_VALIDE("Signé/Validé"),
        REFUSE("Refusé");
        
        private final String libelle;
        
        StatutContrat(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
}