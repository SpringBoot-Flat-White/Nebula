package com.nebula.nebulaCloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data like passwords.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${app.encryption.key:MySecretKey12345}")
    private String secretKey;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts a plain text string.
     * 
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted string
     * @throws Exception If encryption fails
     */
    public String encrypt(String plainText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    /**
     * Decrypts an encrypted string.
     * 
     * @param encryptedText Base64 encoded encrypted string
     * @return Decrypted plain text
     * @throws Exception If decryption fails
     */
    public String decrypt(String encryptedText) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);
        
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, iv.length);
        
        // Extract encrypted data
        byte[] encryptedData = new byte[decodedData.length - GCM_IV_LENGTH];
        System.arraycopy(decodedData, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Generates a secure random password.
     * 
     * @param length Length of the password
     * @return Generated password
     */
    public String generateSecurePassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        
        return password.toString();
    }
}
