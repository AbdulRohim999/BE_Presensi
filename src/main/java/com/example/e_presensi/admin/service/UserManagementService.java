package com.example.e_presensi.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map; // Import untuk Map
import java.util.HashMap; // Import untuk HashMap

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.admin.dto.UserCreateRequest;
import com.example.e_presensi.admin.dto.UserResponse;
import com.example.e_presensi.admin.dto.UserUpdateRequest;
import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.LoginRepository;
import com.example.e_presensi.login.repository.UserProfileRepository;

@Service
public class UserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
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
        
        // Cek apakah email sudah terdaftar
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        
        // Buat user profile baru
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstname(request.getFirstname());
        userProfile.setLastname(request.getLastname());
        userProfile.setEmail(request.getEmail());
        userProfile.setRole("user"); // Default role
        userProfile.setTipeUser(request.getTipeUser());
        userProfile.setBidangKerja(request.getBidangKerja());
        userProfile.setStatus(request.getStatus()); // Tambahkan baris ini untuk mengatur status
        userProfile.setCreateAt(LocalDateTime.now());
        
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        
        // Buat login baru
        Login login = new Login();
        login.setUsername(request.getUsername());
        login.setEmail(request.getEmail());
        login.setPassword(passwordEncoder.encode(request.getPassword()));
        login.setRole("USER"); // Default role
        login.setUserProfile(savedProfile);
        login.setCreateAt(LocalDateTime.now());
        
        loginRepository.save(login);
        
        logger.info("User baru berhasil dibuat dengan email: {}", request.getEmail());
        
        return mapToUserResponse(savedProfile);
    }
    
    private UserResponse mapToUserResponse(UserProfile userProfile) {
        // Hapus baris ini karena variabel request tidak ada dalam metode ini
        // userProfile.setStatus(request.getStatus());
        
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
        user.setUpdateAt(LocalDateTime.now());
        
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
}