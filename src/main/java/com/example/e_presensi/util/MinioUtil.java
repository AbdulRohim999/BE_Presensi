package com.example.e_presensi.util;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUtil {

    private static final Logger logger = LoggerFactory.getLogger(MinioUtil.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Inisialisasi bucket jika belum ada
     */
    public void initBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Bucket {} berhasil dibuat", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error saat inisialisasi bucket: {}", e.getMessage());
        }
    }

    /**
     * Upload file ke MinIO
     * 
     * @param file File yang akan diupload
     * @param fileName Nama file di MinIO
     * @return URL file yang diupload
     */
    public String uploadFile(MultipartFile file, String fileName) {
        try {
            // Cek apakah bucket sudah ada
            initBucket();
            
            // Upload file
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            logger.info("File {} berhasil diupload ke bucket {}", fileName, bucketName);
            
            // Generate URL untuk akses file
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
                    
            return url;
        } catch (Exception e) {
            logger.error("Error saat upload file: {}", e.getMessage());
            throw new RuntimeException("Gagal upload file: " + e.getMessage());
        }
    }

    /**
     * Mendapatkan file dari MinIO
     * 
     * @param fileName Nama file di MinIO
     * @return InputStream dari file
     */
    public InputStream getFile(String fileName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            logger.error("Error saat mendapatkan file: {}", e.getMessage());
            throw new RuntimeException("Gagal mendapatkan file: " + e.getMessage());
        }
    }

    /**
     * Mendapatkan URL untuk akses file
     * 
     * @param fileName Nama file di MinIO
     * @return URL file
     */
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            logger.error("Error saat mendapatkan URL file: {}", e.getMessage());
            throw new RuntimeException("Gagal mendapatkan URL file: " + e.getMessage());
        }
    }

    /**
     * Menghapus file dari MinIO
     * 
     * @param fileName Nama file di MinIO
     */
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            logger.info("File {} berhasil dihapus dari bucket {}", fileName, bucketName);
        } catch (Exception e) {
            logger.error("Error saat menghapus file: {}", e.getMessage());
            throw new RuntimeException("Gagal menghapus file: " + e.getMessage());
        }
    }
}