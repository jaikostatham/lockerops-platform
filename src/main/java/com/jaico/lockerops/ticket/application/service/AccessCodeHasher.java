package com.jaico.lockerops.ticket.application.service;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class AccessCodeHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AccessCodeProperties accessCodeProperties;

    public AccessCodeHasher(AccessCodeProperties accessCodeProperties) {
        this.accessCodeProperties = accessCodeProperties;
    }

    public String hash(String rawAccessCode) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    accessCodeProperties.getHashSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            mac.init(secretKey);

            byte[] digest = mac.doFinal(rawAccessCode.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Access code hashing failed", exception);
        }
    }

    public boolean matches(String rawAccessCode, String storedHash) {
        byte[] rawHashBytes = hash(rawAccessCode).getBytes(StandardCharsets.UTF_8);
        byte[] storedHashBytes = storedHash.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(rawHashBytes, storedHashBytes);
    }

    public String preview(String rawAccessCode) {
        int suffixLength = Math.min(2, rawAccessCode.length());
        String suffix = rawAccessCode.substring(rawAccessCode.length() - suffixLength);

        return "*".repeat(rawAccessCode.length() - suffixLength) + suffix;
    }
}
