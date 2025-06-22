package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "TYPE_SERVICE_PERSONNALISE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeServicePersonnalise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_service")
    private Long idTypeService;

    @NotBlank(message = "Le nom du service est obligatoire")
    @Size(max = 100, message = "Le nom du service ne peut pas dépasser 100 caractères")
    @Column(name = "nom_service", nullable = false, length = 100)
    private String nomService;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La catégorie parente est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_parente", nullable = false)
    private ServiceType categorieParente;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "id_createur", nullable = false)
    private Utilisateur createur;

    @Column(name = "valide_par_admin")
    private Boolean valideParAdmin = false;

    @Column(name = "actif")
    private Boolean actif = true;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public boolean peutEtreUtilise() {
        return actif && valideParAdmin;
    }

    public String getNomComplet() {
        return categorieParente.getLibelle() + " - " + nomService;
    }
}
