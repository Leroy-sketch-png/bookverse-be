package com.example.bookverseserver.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default no-op implementation. Replace in production with real encrypt/decrypt
 * that uses keys from a secrets manager (Vault, AWS KMS, GCP KMS).
 */
@Service
@Slf4j
public class NoOpTokenEncryptionService {
    public String encrypt(String plain) {
        return plain; // TODO: replace with real encryption
    }

    public String decrypt(String cipher) {
        return cipher; // TODO: replace with real decryption
    }
}
