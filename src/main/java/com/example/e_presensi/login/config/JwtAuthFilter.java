package com.example.e_presensi.login.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getServletPath();
        logger.info("Request path: {}", path);
        
        // Allow OPTIONS requests for CORS
        if (request.getMethod().equals("OPTIONS")) {
            logger.info("Permintaan OPTIONS diteruskan");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if path is public
        if (path.contains("/api/auth") || 
            path.contains("/swagger-ui") ||
            path.contains("/v3/api-docs")) {
            logger.info("Path publik diteruskan: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Validate token
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Token diterima: {}", token);
            
            try {
                // Validasi token terlebih dahulu
                boolean isValid = jwtUtil.validateToken(token);
                logger.info("Token valid: {}", isValid);
                
                if (isValid) {
                    // Extract email from token
                    String email = jwtUtil.getEmailFromToken(token);
                    logger.info("Email dari token: {}", email);
                    
                    if (email != null) {
                        // Cari user profile berdasarkan email
                        logger.info("Mencari user dengan email: {}", email);
                        Optional<UserProfile> userProfileOpt = userProfileRepository.findByEmail(email);
                        logger.info("Hasil pencarian user: {}", userProfileOpt.isPresent() ? "Ditemukan" : "Tidak ditemukan");
                        
                        if (userProfileOpt.isPresent()) {
                            UserProfile userProfile = userProfileOpt.get();
                            logger.info("User ditemukan: {} {} dengan role: {}", 
                                userProfile.getFirstname(), 
                                userProfile.getLastname(), 
                                userProfile.getRole());
                            
                            // Tambahkan ID user ke request attribute untuk digunakan di controller
                            request.setAttribute("userId", userProfile.getId_user());
                            logger.info("ID User yang diset: {}", userProfile.getId_user());
                            
                            // Pastikan role tidak null dan tidak kosong
                            String role = userProfile.getRole();
                            if (role == null || role.isEmpty()) {
                                role = "user"; // Default role jika kosong
                                logger.warn("Role kosong, menggunakan default role: user");
                            }
                            
                            // Gunakan role langsung tanpa prefix ROLE_ dan tanpa mengubah ke uppercase
                            logger.info("Authority yang diberikan: {}", role);
                            
                            // Create authentication object and set in SecurityContext
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    email, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                            
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("Authentication berhasil diset di SecurityContext");
                            
                            filterChain.doFilter(request, response);
                            return;
                        } else {
                            logger.warn("User profile tidak ditemukan untuk email: {}", email);
                        }
                    } else {
                        logger.warn("Email tidak dapat diekstrak dari token");
                    }
                } else {
                    logger.warn("Token tidak valid");
                }
            } catch (Exception e) {
                logger.error("Error saat memproses token: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Authorization header tidak valid atau tidak ada");
        }
        
        // Jika sampai di sini, berarti autentikasi gagal
        logger.warn("Autentikasi gagal, mengirim response 401");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"Unauthorized: Token tidak valid atau tidak ada\"}");
    }
    
    /**
     * Metode untuk debugging token JWT
     */
    private void debugToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length == 3) {
                Base64.Decoder decoder = Base64.getUrlDecoder();
                
                String header = new String(decoder.decode(chunks[0]));
                String payload = new String(decoder.decode(chunks[1]));
                
                logger.info("Token header: {}", header);
                logger.info("Token payload: {}", payload);
            }
        } catch (Exception e) {
            logger.error("Error saat mendecode token: {}", e.getMessage());
        }
    }
}