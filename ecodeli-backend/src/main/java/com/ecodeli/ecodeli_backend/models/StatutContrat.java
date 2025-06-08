package com.ecodeli.ecodeli_backend.models;

public enum StatutContrat {
    BROUILLON("Brouillon"),
    ATTENTE_SIGNATURE_COMMERCANT("En attente de signature du commerçant"),
    ATTENTE_SIGNATURE_ADMIN("En attente de validation administrateur"),
    ACTIF("Actif"),
    REFUSE("Refusé"),
    RESILIE("Résilié");

    private final String libelle;

    StatutContrat(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
