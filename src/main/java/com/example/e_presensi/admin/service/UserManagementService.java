package com.example.e_presensi.admin.service;

import java.util.HashMap; // Import untuk HashMap
import java.util.List;
import java.util.Map; // Import untuk Map
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.admin.dto.PasswordChangeRequest;
import com.example.e_presensi.admin.dto.UserCreateRequest;
import com.example.e_presensi.admin.dto.UserResponse;
import com.example.e_presensi.admin.dto.UserUpdateRequest;
import com.example.e_presensi.login.dto.ProfilePhotoResponse;
import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.LoginRepository;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.login.service.ProfilePhotoService;
import com.example.e_presensi.util.DateTimeUtil;

@Service
public class UserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
    @Autowired
    private ProfilePhotoService profilePhotoService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public List<UserResponse> getAllUsers() {
        List<UserProfile> users = userProfileRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getUsersByRole(String role) {
        List<UserProfile> users = userProfileRepository.findByRole(role);
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public UserResponse getUserById(Integer idUser) {
        Optional<UserProfile> userOpt = userProfileRepository.findById(idUser);
        if (!userOpt.isPresent()) {
            return null;
        }
        return mapToUserResponse(userOpt.get());
    }
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Membuat user baru dengan email: {}", request.getEmail());
        
        try {
            // Validasi input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email tidak boleh kosong");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Password tidak boleh kosong");
            }
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("Username tidak boleh kosong");
            }
            
            if (request.getFirstname() == null || request.getFirstname().trim().isEmpty()) {
                throw new IllegalArgumentException("Firstname tidak boleh kosong");
            }
            
            // Validasi format email
            if (!request.getEmail().contains("@")) {
                throw new IllegalArgumentException("Format email tidak valid");
            }
            
            // Cek apakah email sudah terdaftar
            if (userProfileRepository.existsByEmail(request.getEmail().trim())) {
                throw new IllegalArgumentException("Email sudah terdaftar");
            }
            
            // Cek apakah username sudah digunakan
            Optional<Login> existingLogin = loginRepository.findByUsername(request.getUsername().trim());
            if (existingLogin.isPresent()) {
                throw new IllegalArgumentException("Username sudah digunakan");
            }
            
            // Buat user profile baru
            UserProfile userProfile = new UserProfile();
            userProfile.setFirstname(request.getFirstname().trim());
            userProfile.setLastname(request.getLastname() != null ? request.getLastname().trim() : "");
            userProfile.setEmail(request.getEmail().trim());
            userProfile.setRole("user"); // Default role - lowercase
            userProfile.setTipeUser(request.getTipeUser() != null ? request.getTipeUser().trim() : "Dosen");
            userProfile.setBidangKerja(request.getBidangKerja() != null ? request.getBidangKerja().trim() : "");
            userProfile.setStatus(request.getStatus() != null ? request.getStatus().trim() : "Aktif");
            userProfile.setCreateAt(DateTimeUtil.getCurrentDateTimeWIB());
            
            logger.info("Menyimpan user profile untuk email: {}", userProfile.getEmail());
            UserProfile savedProfile = userProfileRepository.save(userProfile);
            logger.info("User profile berhasil disimpan dengan ID: {}", savedProfile.getId_user());
            
            // Buat data login
            Login login = new Login();
            login.setUsername(request.getUsername().trim());
            login.setPassword(passwordEncoder.encode(request.getPassword()));
            login.setRole("user"); // Konsisten dengan UserProfile - lowercase
            login.setEmail(request.getEmail().trim());
            login.setUserProfile(savedProfile);
            login.setCreateAt(DateTimeUtil.getCurrentDateTimeWIB());
            
            logger.info("Menyimpan data login untuk username: {}", login.getUsername());
            loginRepository.save(login);
            logger.info("Data login berhasil disimpan");
            
            logger.info("User baru berhasil dibuat dengan email: {} dan role: {}", request.getEmail(), "user");
            
            return mapToUserResponse(savedProfile);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat membuat user baru: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saat membuat user baru: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal membuat user baru: " + e.getMessage());
        }
    }
    
    public String getUserPhotoUrl(Integer idUser) {
        logger.info("Mendapatkan URL foto profil untuk user ID: {}", idUser);
        
        try {
            ProfilePhotoResponse photoResponse = profilePhotoService.getProfilePhoto(idUser);
            
            if (photoResponse == null) {
                logger.warn("User dengan ID {} tidak ditemukan", idUser);
                return null;
            }
            
            return photoResponse.getFotoProfileUrl();
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan URL foto profil untuk user ID: {}", idUser, e);
            return null;
        }
    }
    
    private UserResponse mapToUserResponse(UserProfile userProfile) {
        // Dapatkan URL foto profil
        String fotoProfileUrl = null;
        try {
            ProfilePhotoResponse photoResponse = profilePhotoService.getProfilePhoto(userProfile.getId_user());
            if (photoResponse != null) {
                fotoProfileUrl = photoResponse.getFotoProfileUrl();
            }
        } catch (Exception e) {
            logger.warn("Gagal mendapatkan URL foto profil untuk user ID: {}", userProfile.getId_user(), e);
        }
        
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
                .fotoProfile(fotoProfileUrl)
                .createdAt(userProfile.getCreateAt())
                .updatedAt(userProfile.getUpdateAt())
                .build();
    }

    /**
     * Memperbarui data pengguna berdasarkan ID
     * 
     * @param idUser ID pengguna yang akan diperbarui
     * @param request Data pengguna yang baru
     * @return UserResponse jika berhasil, null jika pengguna tidak ditemukan
     */
    @Transactional
    public UserResponse updateUser(Integer idUser, UserUpdateRequest request) {
        logger.info("Memperbarui data pengguna dengan ID: {}", idUser);
        
        Optional<UserProfile> userOpt = userProfileRepository.findById(idUser);
        if (!userOpt.isPresent()) {
            logger.warn("Pengguna dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile user = userOpt.get();
        
        // Update data yang diizinkan untuk diubah
        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
        
        if (request.getEmail() != null) {
            // Cek apakah email sudah digunakan oleh user lain
            Optional<UserProfile> existingEmail = userProfileRepository.findByEmail(request.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId_user().equals(idUser)) {
                logger.warn("Email {} sudah digunakan oleh user lain", request.getEmail());
                throw new IllegalArgumentException("Email sudah digunakan oleh user lain");
            }
            
            String oldEmail = user.getEmail();
            String newEmail = request.getEmail();
            
            // Update email di UserProfile
            user.setEmail(newEmail);
            
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
            user.setNip(request.getNip());
        }
        
        if (request.getTipeUser() != null) {
            user.setTipeUser(request.getTipeUser());
        }
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        if (request.getBidangKerja() != null) {
            user.setBidangKerja(request.getBidangKerja());
        }
        
        if (request.getAlamat() != null) {
            user.setAlamat(request.getAlamat());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        // Update waktu perubahan
        user.setUpdateAt(DateTimeUtil.getCurrentDateTimeWIB());
        
        // Simpan perubahan
        UserProfile updatedUser = userProfileRepository.save(user);
        logger.info("Data pengguna dengan ID {} berhasil diperbarui", idUser);
        
        return mapToUserResponse(updatedUser);
    }
    
    /**
     * Menghapus data pengguna berdasarkan ID
     * 
     * @param idUser ID pengguna yang akan dihapus
     * @return true jika berhasil dihapus, false jika pengguna tidak ditemukan
     */
    @Transactional
    public boolean deleteUser(Integer idUser) {
        logger.info("Menghapus data pengguna dengan ID: {}", idUser);
        
        Optional<UserProfile> userOpt = userProfileRepository.findById(idUser);
        if (!userOpt.isPresent()) {
            logger.warn("Pengguna dengan ID {} tidak ditemukan", idUser);
            return false;
        }
        
        UserProfile user = userOpt.get();
        
        // Hapus data login terlebih dahulu (jika ada)
        Optional<Login> loginOpt = loginRepository.findByEmail(user.getEmail());
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            loginRepository.delete(login);
            logger.info("Data login untuk pengguna dengan ID {} berhasil dihapus", idUser);
        }
        
        // Hapus data user profile
        userProfileRepository.delete(user);
        logger.info("Data pengguna dengan ID {} berhasil dihapus", idUser);
        
        return true;
    }
    
    /**
     * Mendapatkan total user berdasarkan tipe_user
     * 
     * @param tipeUser Tipe user (Dosen atau Karyawan)
     * @return Jumlah user dengan tipe_user yang ditentukan
     */
    public long getTotalUserByTipeUser(String tipeUser) {
        logger.info("Mendapatkan total user dengan tipe_user: {}", tipeUser);
        return userProfileRepository.countByTipeUser(tipeUser);
    }
    
    /**
     * Mendapatkan total user untuk semua tipe_user (Dosen dan Karyawan)
     * 
     * @return Map berisi total user untuk setiap tipe_user
     */
    public Map<String, Long> getTotalUserByAllTipeUser() {
        logger.info("Mendapatkan total user untuk semua tipe_user");
        
        Map<String, Long> result = new HashMap<>();
        result.put("Dosen", userProfileRepository.countByTipeUser("Dosen"));
        result.put("Karyawan", userProfileRepository.countByTipeUser("Karyawan"));
        result.put("Total", userProfileRepository.countByTipeUser("Dosen") + 
                          userProfileRepository.countByTipeUser("Karyawan"));
        
        return result;
    }
    
    /**
     * Mengubah password user dari sisi admin
     * 
     * @param idUser ID pengguna yang passwordnya akan diubah
     * @param request Request berisi password baru dan konfirmasi
     * @return true jika berhasil, false jika user tidak ditemukan
     */
    @Transactional
    public boolean changeUserPassword(Integer idUser, PasswordChangeRequest request) {
        logger.info("Mengubah password untuk user ID: {}", idUser);
        
        // Validasi input
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("Password baru tidak boleh kosong");
        }
        
        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password dan konfirmasi password tidak cocok");
        }
        
        // Cari user profile
        Optional<UserProfile> userOpt = userProfileRepository.findById(idUser);
        if (!userOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return false;
        }
        
        UserProfile userProfile = userOpt.get();
        
        // Cari data login berdasarkan email user
        Optional<Login> loginOpt = loginRepository.findByEmail(userProfile.getEmail());
        if (!loginOpt.isPresent()) {
            logger.warn("Data login untuk user dengan ID {} tidak ditemukan", idUser);
            return false;
        }
        
        Login login = loginOpt.get();
        
        // Enkripsi password baru
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        
        // Update password di tabel login
        login.setPassword(encodedPassword);
        loginRepository.save(login);
        
        logger.info("Password untuk user ID {} berhasil diubah", idUser);
        return true;
    }
}