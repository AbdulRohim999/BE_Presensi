package com.example.e_presensi.login.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.login.dto.ProfilePhotoResponse;
import com.example.e_presensi.util.MinioUtil;

@Service
public class ProfilePhotoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfilePhotoService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private MinioUtil minioUtil;
    
    public ProfilePhotoResponse getProfilePhoto(Integer idUser) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        String fotoProfileUrl = null;
        
        // Jika ada foto profile, dapatkan URL-nya
        if (userProfile.getFotoProfile() != null && !userProfile.getFotoProfile().isEmpty()) {
            try {
                fotoProfileUrl = minioUtil.getFileUrl(userProfile.getFotoProfile());
            } catch (Exception e) {
                logger.error("Error saat mendapatkan URL foto: {}", e.getMessage());
            }
        }
        
        return ProfilePhotoResponse.builder()
                .idUser(userProfile.getId_user())
                .fotoProfile(userProfile.getFotoProfile())
                .fotoProfileUrl(fotoProfileUrl)
                .build();
    }
    
    @Transactional
    public ProfilePhotoResponse uploadProfilePhoto(Integer idUser, MultipartFile file) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        try {
            // Validasi tipe file
            String contentType = file.getContentType();
            if (contentType == null || !(contentType.equals("image/jpeg") || 
                                        contentType.equals("image/jpg") || 
                                        contentType.equals("image/png"))) {
                throw new IllegalArgumentException("Tipe file tidak didukung. Hanya file JPG, JPEG, dan PNG yang diperbolehkan.");
            }
            
            // Hapus foto lama jika ada
            if (userProfile.getFotoProfile() != null && !userProfile.getFotoProfile().isEmpty()) {
                try {
                    minioUtil.deleteFile(userProfile.getFotoProfile());
                } catch (Exception e) {
                    logger.warn("Gagal menghapus foto lama: {}", e.getMessage());
                }
            }
            
            // Generate nama file unik
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = "profile-" + idUser + "-" + UUID.randomUUID().toString() + fileExtension;
            
            // Upload file ke MinIO
            String fileUrl = minioUtil.uploadFile(file, fileName);
            
            // Update data user
            userProfile.setFotoProfile(fileName);
            userProfile.setUpdateAt(LocalDateTime.now());
            
            UserProfile updatedProfile = userProfileRepository.save(userProfile);
            logger.info("Foto profil user dengan ID {} berhasil diperbarui", idUser);
            
            return ProfilePhotoResponse.builder()
                    .idUser(updatedProfile.getId_user())
                    .fotoProfile(updatedProfile.getFotoProfile())
                    .fotoProfileUrl(fileUrl)
                    .build();
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saat upload foto profil: {}", e.getMessage());
            throw new RuntimeException("Gagal upload foto profil: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deleteProfilePhoto(Integer idUser) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Hapus foto dari MinIO jika ada
        if (userProfile.getFotoProfile() != null && !userProfile.getFotoProfile().isEmpty()) {
            try {
                minioUtil.deleteFile(userProfile.getFotoProfile());
                
                // Update data user
                userProfile.setFotoProfile(null);
                userProfile.setUpdateAt(LocalDateTime.now());
                userProfileRepository.save(userProfile);
                
                logger.info("Foto profil user dengan ID {} berhasil dihapus", idUser);
            } catch (Exception e) {
                logger.error("Error saat menghapus foto profil: {}", e.getMessage());
                throw new RuntimeException("Gagal menghapus foto profil: " + e.getMessage());
            }
        }
    }
    
    @Transactional
    public ProfilePhotoResponse updateProfilePhoto(Integer idUser, String fotoProfile) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        try {
            // Update data user dengan URL foto yang diberikan
            userProfile.setFotoProfile(fotoProfile);
            userProfile.setUpdateAt(LocalDateTime.now());
            
            UserProfile updatedProfile = userProfileRepository.save(userProfile);
            logger.info("Foto profil user dengan ID {} berhasil diperbarui dengan URL", idUser);
            
            // Dapatkan URL untuk akses foto
            String fotoProfileUrl = null;
            if (fotoProfile != null && !fotoProfile.isEmpty()) {
                try {
                    // Jika fotoProfile sudah berupa URL lengkap, gunakan langsung
                    if (fotoProfile.startsWith("http")) {
                        fotoProfileUrl = fotoProfile;
                    } else {
                        // Jika bukan URL lengkap, coba dapatkan dari MinIO
                        fotoProfileUrl = minioUtil.getFileUrl(fotoProfile);
                    }
                } catch (Exception e) {
                    logger.error("Error saat mendapatkan URL foto: {}", e.getMessage());
                }
            }
            
            return ProfilePhotoResponse.builder()
                    .idUser(updatedProfile.getId_user())
                    .fotoProfile(updatedProfile.getFotoProfile())
                    .fotoProfileUrl(fotoProfileUrl)
                    .build();
            
        } catch (Exception e) {
            logger.error("Error saat update foto profil: {}", e.getMessage());
            throw new RuntimeException("Gagal update foto profil: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Tidak ada ekstensi
        }
        return filename.substring(lastIndexOf);
    }
}