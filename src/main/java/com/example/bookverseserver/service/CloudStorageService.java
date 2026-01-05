package com.example.bookverseserver.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudStorageService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // Upload file lên Cloudinary
            // "folder": "bookverse_avatar" -> Nó sẽ tạo thư mục tên này trên Cloud cho gọn
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookverse_avatar",
                            "resource_type", "auto"
                    ));

            // Trả về URL ảnh online (https://res.cloudinary.com/...)
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
        }
    }
}