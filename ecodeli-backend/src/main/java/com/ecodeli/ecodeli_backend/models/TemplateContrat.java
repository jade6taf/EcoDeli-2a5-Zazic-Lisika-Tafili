package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "TEMPLATE_CONTRAT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateContrat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_template")
    private Integer idTemplate;
    
    @NotBlank(message = "Le nom du template est obligatoire")
    @Column(name = "nom_template", nullable = false, length = 100)
    private String nomTemplate;
    
    @NotBlank(message = "Le contenu du template est obligatoire")
    @Column(name = "contenu_template", columnDefinition = "LONGTEXT")
    private String contenuTemplate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "actif")
    private Boolean actif = true;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_createur")
    private Admin adminCreateur;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}