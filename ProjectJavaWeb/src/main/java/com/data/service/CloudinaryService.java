package com.data.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            // Convert MultipartFile to File
            File convFile = convertMultiPartToFile(file);

            // Upload to Cloudinary with folder organization
            Map<String, Object> uploadResult = cloudinary.uploader().upload(convFile,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto",
                            "public_id", generatePublicId(file.getOriginalFilename())
                    )
            );

            // Clean up temporary file
            convFile.delete();

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generatePublicId(String originalFilename) {
        // Generate unique public_id to avoid conflicts
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        return filename + "_" + timestamp;
    }

    public void deleteFile(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
