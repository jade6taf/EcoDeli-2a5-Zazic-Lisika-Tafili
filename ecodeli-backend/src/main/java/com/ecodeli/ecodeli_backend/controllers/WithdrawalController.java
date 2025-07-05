package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.RetraitDemande;
import com.ecodeli.ecodeli_backend.services.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<?> requestWithdrawal(@Valid @RequestBody WithdrawalRequest request) {
        try {
            RetraitDemande retrait = withdrawalService.requestWithdrawal(
                request.getLivreurId(),
                request.getMontant(),
                request.getIban(),
                request.getNomTitulaire()
            );
            log.info("Demande de retrait créée pour le livreur {} - Montant: {}€",
                    request.getLivreurId(), request.getMontant());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Demande de retrait créée avec succès",
                "retraitId", retrait.getIdRetrait(),
                "montant", retrait.getMontant(),
                "statut", retrait.getStatut()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation pour la demande de retrait: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la création de la demande de retrait", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history/{livreurId}")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getWithdrawalHistory(@PathVariable Integer livreurId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RetraitDemande> retraits = withdrawalService.getWithdrawalHistory(livreurId, pageable);
            return ResponseEntity.ok(Map.of(
                "retraits", retraits.getContent(),
                "totalElements", retraits.getTotalElements(),
                "totalPages", retraits.getTotalPages(),
                "currentPage", retraits.getNumber(),
                "hasNext", retraits.hasNext(),
                "hasPrevious", retraits.hasPrevious()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique des retraits", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/can-withdraw/{livreurId}")
    @PreAuthorize("hasRole('LIVREUR') or hasRole('ADMIN')")
    public ResponseEntity<?> canWithdraw(@PathVariable Integer livreurId,
                                        @RequestParam BigDecimal montant) {
        try {
            Map<String, Object> result = withdrawalService.canWithdraw(livreurId, montant);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de retrait", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    public static class WithdrawalRequest {
        @NotNull(message = "L'ID du livreur est requis")
        private Integer livreurId;

        @NotNull(message = "Le montant est requis")
        @DecimalMin(value = "1.0", message = "Le montant minimum de retrait est de 1€")
        private BigDecimal montant;

        @NotBlank(message = "L'IBAN est requis")
        @Size(min = 8, max = 50, message = "L'IBAN doit contenir entre 8 et 50 caractères")
        private String iban;

        @NotBlank(message = "Le nom du titulaire est requis")
        @Size(min = 2, max = 100, message = "Le nom du titulaire doit contenir entre 2 et 100 caractères")
        private String nomTitulaire;

        public Integer getLivreurId() { return livreurId; }
        public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }
        public BigDecimal getMontant() { return montant; }
        public void setMontant(BigDecimal montant) { this.montant = montant; }
        public String getIban() { return iban; }
        public void setIban(String iban) { this.iban = iban; }
        public String getNomTitulaire() { return nomTitulaire; }
        public void setNomTitulaire(String nomTitulaire) { this.nomTitulaire = nomTitulaire; }
    }
}
