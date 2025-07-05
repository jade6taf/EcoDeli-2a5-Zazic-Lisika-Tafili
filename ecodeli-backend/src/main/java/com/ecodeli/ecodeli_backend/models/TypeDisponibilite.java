package com.ecodeli.ecodeli_backend.models;

public enum TypeDisponibilite {
    DISPONIBLE("Disponible", "#22c55e"),
    OCCUPE("Occupé", "#ef4444"),
    PAUSE("Pause", "#f59e0b");

    private final String libelle;
    private final String couleur;

    TypeDisponibilite(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getCouleur() {
        return couleur;
    }
}
