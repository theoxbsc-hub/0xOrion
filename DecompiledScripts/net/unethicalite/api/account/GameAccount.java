/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.account;

public class GameAccount {
    private final String username;
    private final String password;
    private String auth;

    public GameAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAuth() {
        return this.auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}

