package com.maroon.mixology.exchange.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank
    private String UUID;

    @NotBlank
    @Size(min = 8, max = 256)
    private String password;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}