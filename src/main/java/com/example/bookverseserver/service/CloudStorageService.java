package com.example.bookverseserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudStorageService {

    public String uploadFile(MultipartFile file) {
        // Upload logic
        return "uploaded_url";
    }

    public void deleteFile(String fileUrl) {
        // Delete logic
    }
}
