package com.example.e_presensi.login.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.login.config.JwtUtil;
import com.example.e_presensi.login.dto.UserLoginRequest;
import com.example.e_presensi.login.dto.UserLoginResponse;
import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.LoginRepository;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.util.DateTimeUtil;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        logger.info("Mencoba login dengan email: {}", request.getEmail());
        
        // Validasi input
        if(request.getEmail() == null || request.getEmail().isEmpty() || 
           request.getPassword() == null || request.getPassword().isEmpty()) {
            logger.warn("Email atau password kosong");
            return null;
        }
        
        // Cari user berdasarkan email
        Optional<Login> loginOpt = loginRepository.findByEmail(request.getEmail());
        
        if (!loginOpt.isPresent()) {
            logger.warn("Email tidak ditemukan di tabel Login: {}", request.getEmail());
            
            // Coba cari di UserProfile jika tidak ditemukan di Login
            Optional<UserProfile> userProfileOpt = userProfileRepository.findByEmail(request.getEmail());
            if (userProfileOpt.isPresent()) {
                UserProfile userProfile = userProfileOpt.get();
                
                // Cari login yang terkait dengan userProfile
                Optional<Login> relatedLoginOpt = loginRepository.findByUserProfile(userProfile);
                if (relatedLoginOpt.isPresent()) {
                    Login relatedLogin = relatedLoginOpt.get();
                    
                    // Update email di Login jika berbeda
                    if (!relatedLogin.getEmail().equals(request.getEmail())) {
                        relatedLogin.setEmail(request.getEmail());
                        loginRepository.save(relatedLogin);
                        logger.info("Email pada tabel Login diperbarui ke: {}", request.getEmail());
                    }
                    
                    loginOpt = Optional.of(relatedLogin);
                } else {
                    logger.warn("Tidak ditemukan data Login untuk UserProfile dengan email: {}", request.getEmail());
                    return null;
                }
            } else {
                logger.warn("Email tidak ditemukan di UserProfile: {}", request.getEmail());
                return null;
            }
        }
        
        Login login = loginOpt.get();
        logger.debug("Login data ditemukan untuk email: {}", request.getEmail());
        
        try {
            // Verifikasi password
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), login.getPassword());
            logger.debug("Hasil verifikasi password: {}", passwordMatches);
            
            // TEMPORARY SOLUTION untuk debugging (JANGAN GUNAKAN DI PRODUCTION)
            // Jika password match dengan BCrypt gagal, coba dengan perbandingan langsung
            if (!passwordMatches) {
                boolean plainMatch = request.getPassword().equals(login.getPassword());
                logger.warn("Mencoba perbandingan password langsung: {}", plainMatch);
                
                if (plainMatch) {
                    logger.warn("PASSWORD TIDAK TERENKRIPSI DI DATABASE! Harap segera perbaiki untuk keamanan.");
                    passwordMatches = true;
                    
                    // Update password dengan enkripsi (opsional)
                    login.setPassword(passwordEncoder.encode(request.getPassword()));
                    loginRepository.save(login);
                    logger.info("Password diupdate dengan enkripsi BCrypt");
                }
            }
            
            if (passwordMatches) {
                UserProfile userProfile = login.getUserProfile();
                
                if(userProfile == null) {
                    logger.error("UserProfile tidak ditemukan untuk login ID: {}", login.getId_login());
                    
                    // Coba cari user profile berdasarkan email
                    Optional<UserProfile> profileOpt = userProfileRepository.findByEmail(login.getEmail());
                    if (profileOpt.isPresent()) {
                        userProfile = profileOpt.get();
                        logger.info("UserProfile ditemukan menggunakan email");
                        
                        // Perbaiki relasi
                        login.setUserProfile(userProfile);
                        loginRepository.save(login);
                    } else {
                        return null;
                    }
                }
                
                // Pastikan email di UserProfile dan Login konsisten
                if (!userProfile.getEmail().equals(login.getEmail())) {
                    logger.warn("Email tidak konsisten antara UserProfile ({}) dan Login ({})", 
                               userProfile.getEmail(), login.getEmail());
                    
                    // Update email di Login untuk konsistensi
                    login.setEmail(userProfile.getEmail());
                    loginRepository.save(login);
                    logger.info("Email di Login diperbarui untuk konsistensi dengan UserProfile");
                }
                
                // Generate token JWT
                String token = jwtUtil.generateToken(login.getEmail());
                logger.debug("Token JWT berhasil dibuat");
                
                // Membuat response
                return new UserLoginResponse(
                    userProfile.getId_user(),
                    userProfile.getFirstname(),
                    userProfile.getLastname(),
                    userProfile.getEmail(),
                    userProfile.getRole(),
                    userProfile.getTipeUser(),
                    userProfile.getStatus(),
                    token
                );
            } else {
                logger.warn("Password tidak cocok untuk email: {}", request.getEmail());
            }
        } catch (Exception e) {
            logger.error("Error saat proses login: {}", e.getMessage(), e);
        }
        
        // Jika login gagal
        return null;
    }
    
    // Metode untuk inisialisasi user test jika diperlukan
    public void initTestUser() {
        // Cek apakah user test sudah ada
        String testEmail = "test@example.com";
        Optional<Login> existingUser = loginRepository.findByEmail(testEmail);
        
        if (existingUser.isPresent()) {
            logger.info("Test user sudah ada: {}", testEmail);
            return;
        }
        
        try {
            // Buat user profile baru
            UserProfile profile = new UserProfile();
            profile.setFirstname("Test");
            profile.setLastname("User");
            profile.setEmail(testEmail);
            profile.setRole("User");
            profile.setStatus("Dosen");
            profile.setNip("12345");
            profile.setTipeUser("Informatika");
            profile.setCreateAt(DateTimeUtil.getCurrentDateTimeWIB());
            
            userProfileRepository.save(profile);
            
            // Buat data login
            Login login = new Login();
            login.setUsername(testEmail);
            login.setPassword(passwordEncoder.encode("password123"));
            login.setRole("User");
            login.setUserProfile(profile);
            login.setCreateAt(DateTimeUtil.getCurrentDateTimeWIB());
            
            loginRepository.save(login);
            
            logger.info("Test user berhasil dibuat: {} / password123", testEmail);
        } catch (Exception e) {
            logger.error("Error saat membuat test user: {}", e.getMessage(), e);
        }
    }
}