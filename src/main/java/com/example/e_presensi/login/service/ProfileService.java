package com.example.e_presensi.login.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.login.dto.ProfileRequest;
import com.example.e_presensi.login.dto.ProfileResponse;
import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.LoginRepository;
import com.example.e_presensi.login.repository.UserProfileRepository;

@Service
public class ProfileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public ProfileResponse getUserProfile(Integer idUser) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        return mapToResponse(userProfile);
    }
    
    @Transactional
    public ProfileResponse updateUserProfile(Integer idUser, ProfileRequest request) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Update data yang diizinkan untuk diubah
        if (request.getFirstname() != null) {
            userProfile.setFirstname(request.getFirstname());
        }
        
        if (request.getLastname() != null) {
            userProfile.setLastname(request.getLastname());
        }
        
        // Tambahkan tempat tanggal lahir
        if (request.getTempatTanggalLahir() != null) {
            userProfile.setTempatTanggalLahir(request.getTempatTanggalLahir());
        }
        
        if (request.getEmail() != null) {
            // Cek apakah email sudah digunakan oleh user lain
            Optional<UserProfile> existingEmail = userProfileRepository.findByEmail(request.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId_user().equals(idUser)) {
                logger.warn("Email {} sudah digunakan oleh user lain", request.getEmail());
                throw new IllegalArgumentException("Email sudah digunakan oleh user lain");
            }
            
            String oldEmail = userProfile.getEmail();
            String newEmail = request.getEmail();
            
            // Update email di UserProfile
            userProfile.setEmail(newEmail);
            
            // Update juga email di tabel Login
            Optional<Login> loginOpt = loginRepository.findByEmail(oldEmail);
            if (loginOpt.isPresent()) {
                Login login = loginOpt.get();
                login.setEmail(newEmail);
                loginRepository.save(login);
                logger.info("Email pada tabel Login berhasil diperbarui dari {} ke {}", oldEmail, newEmail);
            } else {
                logger.warn("Data Login untuk email {} tidak ditemukan", oldEmail);
            }
        }
        
        if (request.getNip() != null) {
            userProfile.setNip(request.getNip());
        }
        
        if (request.getTipeUser() != null) {
            userProfile.setTipeUser(request.getTipeUser());
        }
        
        if (request.getStatus() != null) {
            userProfile.setStatus(request.getStatus());
        }
        
        // Tambahkan bidang kerja
        if (request.getBidangKerja() != null) {
            userProfile.setBidangKerja(request.getBidangKerja());
        }
        
        // Tambahkan alamat
        if (request.getAlamat() != null) {
            userProfile.setAlamat(request.getAlamat());
        }
        
        // Tambahkan nomor telepon
        if (request.getPhoneNumber() != null) {
            userProfile.setPhoneNumber(request.getPhoneNumber());
        }
        
        // Update waktu perubahan
        userProfile.setUpdateAt(LocalDateTime.now());
        
        // Simpan perubahan
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        logger.info("Profil user dengan ID {} berhasil diperbarui", idUser);
        
        return mapToResponse(updatedProfile);
    }
    
    private ProfileResponse mapToResponse(UserProfile userProfile) {
        return ProfileResponse.builder()
                .idUser(userProfile.getId_user())
                .firstname(userProfile.getFirstname())
                .lastname(userProfile.getLastname())
                .tempatTanggalLahir(userProfile.getTempatTanggalLahir())
                .email(userProfile.getEmail())
                .role(userProfile.getRole())
                .nip(userProfile.getNip())
                .tipeUser(userProfile.getTipeUser())
                .status(userProfile.getStatus())
                .bidangKerja(userProfile.getBidangKerja())
                .alamat(userProfile.getAlamat())
                .phoneNumber(userProfile.getPhoneNumber())
                .createdAt(userProfile.getCreateAt())
                .updatedAt(userProfile.getUpdateAt())
                .build();
    }

    @Transactional
    public boolean changePassword(Integer idUser, String oldPassword, String newPassword) {
        // Cari user profile berdasarkan ID
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return false;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Cari data login terkait
        Optional<Login> loginOpt = loginRepository.findByUserProfile(userProfile);
        if (!loginOpt.isPresent()) {
            logger.warn("Data login untuk user ID {} tidak ditemukan", idUser);
            return false;
        }
        
        Login login = loginOpt.get();
        
        // Verifikasi password lama
        boolean passwordMatches = passwordEncoder.matches(oldPassword, login.getPassword());
        
        // TEMPORARY SOLUTION untuk debugging (JANGAN GUNAKAN DI PRODUCTION)
        // Jika password match dengan BCrypt gagal, coba dengan perbandingan langsung
        if (!passwordMatches) {
            boolean plainMatch = oldPassword.equals(login.getPassword());
            if (plainMatch) {
                logger.warn("PASSWORD TIDAK TERENKRIPSI DI DATABASE! Harap segera perbaiki untuk keamanan.");
                passwordMatches = true;
            }
        }
        
        if (!passwordMatches) {
            logger.warn("Password lama tidak cocok untuk user ID: {}", idUser);
            return false;
        }
        
        // Update password dengan enkripsi
        login.setPassword(passwordEncoder.encode(newPassword));
        loginRepository.save(login);
        
        logger.info("Password berhasil diubah untuk user ID: {}", idUser);
        return true;
    }
}