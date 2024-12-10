package com.exemple.pfa.model;

public class RegisterRes {
    private String email;
    private String token;

    public RegisterRes(String email, String token) {
        this.email = email;
        this.token = token;
    }

    // Getters et Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}