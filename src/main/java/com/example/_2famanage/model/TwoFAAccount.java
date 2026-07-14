package com.example._2famanage.model;

public class TwoFAAccount {
    private String name;
    private String secret;

    public TwoFAAccount() {
    }

    public TwoFAAccount(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
