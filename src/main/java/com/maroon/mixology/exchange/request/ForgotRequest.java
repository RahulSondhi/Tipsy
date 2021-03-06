package com.maroon.mixology.exchange.request;

import javax.validation.constraints.NotBlank;

public class ForgotRequest {
    @NotBlank
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
