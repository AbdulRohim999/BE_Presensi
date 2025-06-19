package com.example.e_presensi.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.admin.dto.AdminCreateRequest;
import com.example.e_presensi.admin.dto.UserResponse;
import com.example.e_presensi.admin.dto.AdminUpdateRequest;
import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.LoginRepository;
import com.example.e_presensi.login.repository.UserProfileRepository;

@Service
public class AdminManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public List<UserResponse> getAllAdmins() {
        List<UserProfile> admins = userProfileRepository.findAll().stream()
                .filter(user -> "admin".equals(user.getRole().toLowerCase()))
                .collect(Collectors.toList());
        
        return admins.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserResponse createAdmin(AdminCreateRequest request) {
        // Validasi input
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email tidak boleh kosong");
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh kosong");
        }
        
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        
        if (request.getFirstname() == null || request.getFirstname().isEmpty()) {
            throw new IllegalArgumentException("Firstname tidak boleh kosong");
        }
        
        // Cek apakah email sudah terdaftar
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        
        // Buat user profile baru dengan role ADMIN
        UserProfile adminProfile = new UserProfile();
        adminProfile.setFirstname(request.getFirstname());
        adminProfile.setLastname(request.getLastname() != null ? request.getLastname() : "");
        adminProfile.setEmail(request.getEmail());
        adminProfile.setRole("admin"); // Set role admin dengan huruf kecil
        adminProfile.setCreateAt(LocalDateTime.now());
        
        UserProfile savedProfile = userProfileRepository.save(adminProfile);
        
        // Buat login baru
        Login login = new Login();
        login.setUsername(request.getUsername());
        login.setEmail(request.getEmail());
        login.setPassword(passwordEncoder.encode(request.getPassword()));
        login.setRole("admin"); // Set role admin dengan huruf kecil
        login.setUserProfile(savedProfile);
        login.setCreateAt(LocalDateTime.now());
        
        loginRepository.save(login);
        
        logger.info("Admin baru berhasil dibuat dengan email: {}", request.getEmail());
        
        return mapToUserResponse(savedProfile);
    }
    
    /**
     * Mengubah UserProfile menjadi UserResponse
     * 
     * @param userProfile UserProfile yang akan diubah
     * @return UserResponse hasil konversi
     */
    private UserResponse mapToUserResponse(UserProfile userProfile) {
        return UserResponse.builder()
                .idUser(userProfile.getId_user())
                .firstname(userProfile.getFirstname())
                .lastname(userProfile.getLastname())
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

    /**
     * Mendapatkan detail admin berdasarkan ID
     * 
     * @param idAdmin ID admin yang akan dicari
     * @return UserResponse jika admin ditemukan, null jika tidak ditemukan
     */
    public UserResponse getAdminById(Integer idAdmin) {
        logger.info("Mencari admin dengan ID: {}", idAdmin);
        
        Optional<UserProfile> adminOpt = userProfileRepository.findById(idAdmin);
        if (!adminOpt.isPresent()) {
            logger.warn("Admin dengan ID {} tidak ditemukan", idAdmin);
            return null;
        }
        
        UserProfile admin = adminOpt.get();
        // Pastikan yang dicari adalah admin
        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            logger.warn("User dengan ID {} bukan admin", idAdmin);
            return null;
        }
        
        return mapToUserResponse(admin);
    }

    /**
     * Mendapatkan jumlah admin yang terdaftar dalam sistem
     * 
     * @return Jumlah admin
     */
    public long getAdminCount() {
        logger.info("Menghitung jumlah admin");
        return userProfileRepository.findAll().stream()
                .filter(user -> "admin".equals(user.getRole().toLowerCase()))
                .count();
    }

    /**
     * Mengubah data admin berdasarkan ID
     * 
     * @param idAdmin ID admin yang akan diubah
     * @param request Data admin yang baru
     * @return UserResponse jika berhasil diubah, null jika admin tidak ditemukan
     */
    @Transactional
    public UserResponse updateAdmin(Integer idAdmin, AdminUpdateRequest request) {
        logger.info("Mengubah data admin dengan ID: {}", idAdmin);
        
        // Cari admin berdasarkan ID
        Optional<UserProfile> adminOpt = userProfileRepository.findById(idAdmin);
        if (!adminOpt.isPresent()) {
            logger.warn("Admin dengan ID {} tidak ditemukan", idAdmin);
            return null;
        }
        
        UserProfile admin = adminOpt.get();
        // Pastikan yang diubah adalah admin
        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            logger.warn("User dengan ID {} bukan admin", idAdmin);
            return null;
        }
        
        // Cek apakah email sudah terdaftar oleh user lain
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
                && !request.getEmail().equals(admin.getEmail()) 
                && userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar oleh user lain");
        }
        
        // Update data admin
        if (request.getFirstname() != null && !request.getFirstname().isEmpty()) {
            admin.setFirstname(request.getFirstname());
        }
        
        if (request.getLastname() != null && !request.getLastname().isEmpty()) {
            admin.setLastname(request.getLastname());
        }
        
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            admin.setEmail(request.getEmail());
            
            // Update email di login juga
            Optional<Login> loginOpt = loginRepository.findByUserProfile(admin);
            if (loginOpt.isPresent()) {
                Login login = loginOpt.get();
                login.setEmail(request.getEmail());
                loginRepository.save(login);
            }
        }
        
        if (request.getPhoneNumber() != null) {
            admin.setPhoneNumber(request.getPhoneNumber());
        }
        
        if (request.getAlamat() != null) {
            admin.setAlamat(request.getAlamat());
        }
        
        if (request.getStatus() != null) {
            admin.setStatus(request.getStatus());
        }
        
        admin.setUpdateAt(LocalDateTime.now());
        UserProfile updatedAdmin = userProfileRepository.save(admin);
        
        // Update username dan password jika ada
        Optional<Login> loginOpt = loginRepository.findByUserProfile(admin);
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                login.setUsername(request.getUsername());
            }
            
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                login.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            
            loginRepository.save(login);
        }
        
        logger.info("Admin dengan ID {} berhasil diubah", idAdmin);
        
        return mapToUserResponse(updatedAdmin);
    }
    
    /**
     * Menghapus admin berdasarkan ID
     * 
     * @param idAdmin ID admin yang akan dihapus
     * @return true jika berhasil dihapus, false jika admin tidak ditemukan
     */
    @Transactional
    public boolean deleteAdmin(Integer idAdmin) {
        logger.info("Menghapus admin dengan ID: {}", idAdmin);
        
        // Cari admin berdasarkan ID
        Optional<UserProfile> adminOpt = userProfileRepository.findById(idAdmin);
        if (!adminOpt.isPresent()) {
            logger.warn("Admin dengan ID {} tidak ditemukan", idAdmin);
            return false;
        }
        
        UserProfile admin = adminOpt.get();
        // Pastikan yang dihapus adalah admin
        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            logger.warn("User dengan ID {} bukan admin", idAdmin);
            return false;
        }
        
        // Hapus login terlebih dahulu
        Optional<Login> loginOpt = loginRepository.findByUserProfile(admin);
        if (loginOpt.isPresent()) {
            loginRepository.delete(loginOpt.get());
        }
        
        // Hapus user profile
        userProfileRepository.delete(admin);
        
        logger.info("Admin dengan ID {} berhasil dihapus", idAdmin);
        
        return true;
    }
}