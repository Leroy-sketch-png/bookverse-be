package com.example.bookverseserver.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * No-op implementation for DEVELOPMENT ONLY.
 * 
 * In production, use AesTokenEncryptionService which provides
 * real AES-256-GCM encryption for OAuth refresh tokens.
 * 
 * WARNING: This service stores tokens in PLAINTEXT. Never use in production.
 */
@Service
@Profile("!prod")
@Slf4j
public class NoOpTokenEncryptionService implements TokenEncryptionService {
    
    public NoOpTokenEncryptionService() {
        log.warn("⚠️  NoOpTokenEncryptionService active - tokens will NOT be encrypted. DO NOT USE IN PRODUCTION.");
    }
    
    @Override
    public String encrypt(String plain) {
        return plain;
    }

    @Override
    public String decrypt(String cipher) {
        return cipher;
    }
}
