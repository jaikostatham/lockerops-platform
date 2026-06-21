package com.jaico.lockerops.ticket.application.service;

import com.jaico.lockerops.ticket.domain.model.AccessCode;

public class IssuedAccessCode {

    private final AccessCode accessCode;
    private final String rawAccessCode;

    public IssuedAccessCode(AccessCode accessCode, String rawAccessCode) {
        this.accessCode = accessCode;
        this.rawAccessCode = rawAccessCode;
    }

    public AccessCode getAccessCode() {
        return accessCode;
    }

    public String getRawAccessCode() {
        return rawAccessCode;
    }
}
