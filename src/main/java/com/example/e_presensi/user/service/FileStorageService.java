package com.example.e_presensi.user.service;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Validasi file
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File tidak boleh kosong");
            }
            
            // Validasi tipe file
            String contentType = file.getContentType();
            if (contentType == null || !isValidFileType(contentType)) {
                throw new IllegalArgumentException("Tipe file tidak didukung. Gunakan PDF, DOC, DOCX, XLS, XLSX, atau gambar");
            }
            
            // Generate nama file unik
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            String objectName = folder + "/" + newFilename;
            
            // Upload ke MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
            
            logger.info("File berhasil diupload: {}", objectName);

            // Generate URL presigned untuk akses file
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS) // URL berlaku selama 7 hari
                    .build());

            return url;
            
        } catch (Exception e) {
            logger.error("Error saat upload file", e);
            throw new RuntimeException("Gagal mengupload file: " + e.getMessage());
        }
    }
    
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            logger.info("File berhasil dihapus: {}", objectName);
        } catch (Exception e) {
            logger.error("Error saat menghapus file", e);
            throw new RuntimeException("Gagal menghapus file: " + e.getMessage());
        }
    }
    
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            logger.error("Error saat mengambil file", e);
            throw new RuntimeException("Gagal mengambil file: " + e.getMessage());
        }
    }
    
    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("application/pdf") ||
               contentType.startsWith("application/msword") ||
               contentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.startsWith("application/vnd.ms-excel") ||
               contentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
               contentType.startsWith("image/");
    }
} 