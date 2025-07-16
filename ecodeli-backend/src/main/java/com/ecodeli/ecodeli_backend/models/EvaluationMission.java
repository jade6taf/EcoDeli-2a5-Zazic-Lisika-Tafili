package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "EVALUATION_MISSION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationMission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation")
    private Long idEvaluation;
    
    @OneToOne
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;
    
    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Prestataire prestataire;
    
    @NotNull(message = "La note globale est obligatoire")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @Column(name = "note_globale", nullable = false)
    private Integer noteGlobale;
    
    @NotNull(message = "La note qualité est obligatoire")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @Column(name = "note_qualite", nullable = false)
    private Integer noteQualite;
    
    @NotNull(message = "La note délais est obligatoire")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @Column(name = "note_delais", nullable = false)
    private Integer noteDelais;
    
    @NotNull(message = "La note communication est obligatoire")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @Column(name = "note_communication", nullable = false)
    private Integer noteCommunication;
    
    @NotNull(message = "La note professionnalisme est obligatoire")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @Column(name = "note_professionnalisme", nullable = false)
    private Integer noteProfessionnalisme;
    
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    @Column(name = "commentaire", length = 500)
    private String commentaire;
    
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (dateEvaluation == null) {
            dateEvaluation = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    /**
     * Calcule la note moyenne de cette évaluation
     */
    public Double getNoteMoyenne() {
        if (noteQualite == null || noteDelais == null || 
            noteCommunication == null || noteProfessionnalisme == null) {
            return null;
        }
        return (noteQualite + noteDelais + noteCommunication + noteProfessionnalisme) / 4.0;
    }
    
    /**
     * Retourne le label textuel de la note globale
     */
    public String getNoteGlobaleLabel() {
        if (noteGlobale == null) return "Non noté";
        
        switch (noteGlobale) {
            case 5: return "Excellent";
            case 4: return "Très bien";
            case 3: return "Bien";
            case 2: return "Passable";
            case 1: return "Insuffisant";
            default: return "Non noté";
        }
    }
    
    /**
     * Vérifie si l'évaluation est complète
     */
    public boolean isComplete() {
        return noteGlobale != null && noteQualite != null && 
               noteDelais != null && noteCommunication != null && 
               noteProfessionnalisme != null;
    }
    
    /**
     * Vérifie si l'évaluation est positive (>= 4)
     */
    public boolean isPositive() {
        return noteGlobale != null && noteGlobale >= 4;
    }
    
    /**
     * Retourne le sentiment général de l'évaluation
     */
    public String getSentiment() {
        if (noteGlobale == null) return "NEUTRE";
        
        if (noteGlobale >= 4) return "POSITIF";
        if (noteGlobale >= 3) return "NEUTRE";
        return "NEGATIF";
    }
}