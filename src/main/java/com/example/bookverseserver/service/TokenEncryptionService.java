package com.example.bookverseserver.service;

/**
 * Service for encrypting/decrypting sensitive tokens (OAuth refresh tokens, etc.)
 * before storing in the database.
 * 
 * Implementations:
 * - NoOpTokenEncryptionService: Development only (returns plaintext)
 * - AesTokenEncryptionService: Production (AES-256-GCM encryption)
 */
public interface TokenEncryptionService {
    
    /**
     * Encrypt a plaintext token for secure storage.
     * @param plainText the token to encrypt
     * @return encrypted token (Base64 encoded)
     */
    String encrypt(String plainText);
    
    /**
     * Decrypt an encrypted token.
     * @param cipherText the encrypted token (Base64 encoded)
     * @return decrypted plaintext token
     */
    String decrypt(String cipherText);
}
