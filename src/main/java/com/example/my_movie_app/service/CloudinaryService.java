package com.example.my_movie_app.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;


    public String uploadImage(MultipartFile file) {

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "cinema"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload image failed");
        }
    }

    public String uploadBytes(byte[] data, String fileName) {
        try {

            Map uploadResult = cloudinary.uploader().upload(
                    data,
                    ObjectUtils.asMap(
                            "public_id", "cinema/qrcode/" + fileName,
                            "overwrite", true
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload QR failed");
        }
    }


    public void deleteImage(String publicId) {

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Delete image failed");
        }
    }
}