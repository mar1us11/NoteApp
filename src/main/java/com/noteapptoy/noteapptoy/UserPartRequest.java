package com.noteapptoy.noteapptoy;

import jakarta.validation.constraints.Size;

public class UserPartRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 8, max = 100)
    private String password;

    public UserPartRequest() {}

    public UserPartRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

