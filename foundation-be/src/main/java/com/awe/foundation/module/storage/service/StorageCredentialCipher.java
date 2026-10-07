package com.awe.foundation.module.storage.service;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 云存储认证信息加解密
 */
@Component
public class StorageCredentialCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SecretKeySpec secretKey;

    public StorageCredentialCipher(@Value("${foundation.storage.secret-key:change-me-foundation-storage-key}") String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[32];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, normalized.length));
        this.secretKey = new SecretKeySpec(normalized, "AES");
    }

    /**
     * 加密认证信息
     *
     * @param plainText 明文
     * @return 密文
     */
    public String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
    }

    /**
     * 解密认证信息
     *
     * @param cipherText 密文
     * @return 明文
     */
    public String decrypt(String cipherText) {
        if (StringUtils.isBlank(cipherText)) {
            return null;
        }
        try {
            byte[] encrypted = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[IV_LENGTH];
            byte[] payload = new byte[encrypted.length - IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encrypted, IV_LENGTH, payload, 0, payload.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
    }

}
