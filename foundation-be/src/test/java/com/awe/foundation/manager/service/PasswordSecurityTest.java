package com.awe.foundation.manager.service;

import cn.dev33.satoken.secure.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PasswordSecurityTest {

    private static final String RAW_PASSWORD = "ChangeMe123!";

    @Test
    void shouldVerifyFrontendSha256DigestWithBcryptHash() {
        String passwordDigest = sha256(RAW_PASSWORD);
        String storedPassword = BCrypt.hashpw(passwordDigest);
        log.info(storedPassword);

        assertNotEquals(passwordDigest, storedPassword);
        assertTrue(BCrypt.checkpw(passwordDigest, storedPassword));
    }

    @Test
    void shouldRejectWrongPasswordDigest() {
        String storedPassword = BCrypt.hashpw(sha256(RAW_PASSWORD));

        assertFalse(BCrypt.checkpw(sha256("WrongPassword!"), storedPassword));
    }

    @Test
    void shouldRejectRawPasswordWhenFrontendDigestIsRequired() {
        String storedPassword = BCrypt.hashpw(sha256(RAW_PASSWORD));

        assertFalse(BCrypt.checkpw(RAW_PASSWORD, storedPassword));
    }

    private String sha256(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexDigest = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hexDigest.append(String.format("%02x", value));
            }
            return hexDigest.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }
}
