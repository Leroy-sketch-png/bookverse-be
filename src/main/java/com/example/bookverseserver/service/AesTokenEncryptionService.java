package com.example.bookverseserver.service;

import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Production-grade AES-256-GCM token encryption service.
 * 
 * Uses authenticated encryption to protect OAuth refresh tokens
 * and other sensitive data stored in the database.
 * 
 * Active only in 'prod' profile. For development, NoOpTokenEncryptionService is used.
 */
@Service
@Profile("prod")
@Slf4j
public class AesTokenEncryptionService implements TokenEncryptionService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit auth tag
    
    @Value("${app.security.token-encryption-key:}")
    private String encryptionKeyHex;
    
    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @PostConstruct
    public void init() {
        if (encryptionKeyHex == null || encryptionKeyHex.isBlank()) {
            throw new IllegalStateException(
                "SECURITY ERROR: app.security.token-encryption-key is not configured. " +
                "This is required for production. Set a 64-character hex string (256-bit key)."
            );
        }
        
        if (encryptionKeyHex.length() != 64) {
            throw new IllegalStateException(
                "SECURITY ERROR: app.security.token-encryption-key must be exactly 64 hex characters (256-bit key). " +
                "Current length: " + encryptionKeyHex.length()
            );
        }
        
        try {
            byte[] keyBytes = hexToBytes(encryptionKeyHex);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            log.info("AES token encryption initialized successfully");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize token encryption key", e);
        }
    }
    
    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            
            // Prepend IV to ciphertext (IV || ciphertext)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Token encryption failed", e);
            throw new AppException(ErrorCode.TOKEN_ENCRYPTION_FAILED);
        }
    }
    
    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            
            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt
            byte[] plainText = cipher.doFinal(encrypted);
            return new String(plainText);
        } catch (Exception e) {
            log.error("Token decryption failed", e);
            throw new AppException(ErrorCode.TOKEN_DECRYPTION_FAILED);
        }
    }
    
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
