package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONTRAT_HISTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_history")
    private Integer idHistory;

    @ManyToOne
    @JoinColumn(name = "id_contrat", nullable = false)
    private Contrat contrat;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_precedent")
    private StatutContrat statutPrecedent;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_nouveau")
    private StatutContrat statutNouveau;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur_action")
    private Utilisateur utilisateurAction;

    @Column(name = "date_action")
    private LocalDateTime dateAction = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }

    public ContratHistory(Contrat contrat, StatutContrat statutPrecedent, StatutContrat statutNouveau,
                         String commentaire, Utilisateur utilisateurAction) {
        this.contrat = contrat;
        this.statutPrecedent = statutPrecedent;
        this.statutNouveau = statutNouveau;
        this.commentaire = commentaire;
        this.utilisateurAction = utilisateurAction;
        this.dateAction = LocalDateTime.now();
    }
}
