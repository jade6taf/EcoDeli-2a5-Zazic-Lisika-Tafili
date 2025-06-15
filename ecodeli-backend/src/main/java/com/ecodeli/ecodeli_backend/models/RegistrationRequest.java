package com.ecodeli.ecodeli_backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrationRequest {

    @JsonProperty("utilisateur")
    private Utilisateur utilisateur;

    @JsonProperty("hcaptchaToken")
    private String hcaptchaToken;

    public RegistrationRequest() {}

    public RegistrationRequest(Utilisateur utilisateur, String hcaptchaToken) {
        this.utilisateur = utilisateur;
        this.hcaptchaToken = hcaptchaToken;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getHcaptchaToken() {
        return hcaptchaToken;
    }

    public void setHcaptchaToken(String hcaptchaToken) {
        this.hcaptchaToken = hcaptchaToken;
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "utilisateur=" + utilisateur +
                ", hcaptchaToken='" + (hcaptchaToken != null ? "[PROTECTED]" : "null") + '\'' +
                '}';
    }
}
