package com.jaico.lockerops.ticket.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TicketCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PREFIX = "TCK-";
    private static final int CODE_LENGTH = 8;

    public String generate() {
        StringBuilder code = new StringBuilder(PREFIX);

        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)]);
        }

        return code.toString();
    }
}
