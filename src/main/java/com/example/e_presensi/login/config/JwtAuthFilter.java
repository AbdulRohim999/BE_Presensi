package com.example.e_presensi.login.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
        
        // --- Pemeriksaan Awal ---
        if (isPublicPath(request.getServletPath()) || "OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header tidak valid atau tidak ada.");
            sendUnauthorizedResponse(response, "Authorization header tidak valid atau tidak ada.");
            return;
        }

        jwt = authHeader.substring(7);
        try {
            userEmail = jwtUtil.getEmailFromToken(jwt);
        } catch (Exception e) {
            logger.error("Gagal mengekstrak email dari token.", e);
            sendUnauthorizedResponse(response, "Token tidak valid.");
            return;
        }
        
        // --- Jika email ada dan belum ada autentikasi ---
        if (StringUtils.isNotBlank(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<UserProfile> userProfileOpt = userProfileRepository.findByEmail(userEmail);
            
            if (userProfileOpt.isPresent() && jwtUtil.validateToken(jwt)) {
                UserProfile userProfile = userProfileOpt.get();
                String role = StringUtils.isNotBlank(userProfile.getRole()) ? userProfile.getRole().toLowerCase().trim() : "user";

                // Set user ID in request attribute
                request.setAttribute("userId", userProfile.getId_user());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("User '{}' dengan role '{}' berhasil diautentikasi.", userEmail, role);
            } else {
                logger.warn("User tidak ditemukan atau token tidak valid untuk email: {}", userEmail);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return path.contains("/api/auth") || 
               path.contains("/swagger-ui") ||
               path.contains("/v3/api-docs") ||
               path.contains("/swagger-resources") ||
               path.contains("/webjars") ||
               // Public running text endpoints
               path.equals("/api/running-text") ||
               path.startsWith("/api/running-text/") ||
               path.equals("/api/user/informasi/running-text/semua");
    }
    
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        logger.warn("Mengirim response 401: {}", message);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"" + message + "\"}");
        response.getWriter().flush();
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