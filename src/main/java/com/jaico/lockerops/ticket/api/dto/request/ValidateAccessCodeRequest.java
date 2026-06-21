package com.jaico.lockerops.ticket.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidateAccessCodeRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
