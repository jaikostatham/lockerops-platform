package com.jaico.lockerops.ticket.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccessCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccessCodeProperties accessCodeProperties;

    public AccessCodeGenerator(AccessCodeProperties accessCodeProperties) {
        this.accessCodeProperties = accessCodeProperties;
    }

    public String generate() {
        int length = Math.max(6, accessCodeProperties.getLength());
        StringBuilder code = new StringBuilder(length);

        for (int index = 0; index < length; index++) {
            code.append(ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)]);
        }

        return code.toString();
    }
}
