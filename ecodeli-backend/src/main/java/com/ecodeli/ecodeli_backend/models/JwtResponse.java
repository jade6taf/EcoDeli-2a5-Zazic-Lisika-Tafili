package com.ecodeli.ecodeli_backend.models;

public class JwtResponse {
    private String token;
    private Utilisateur user;

    public JwtResponse() {
    }

    public JwtResponse(String token, Utilisateur user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }
}
