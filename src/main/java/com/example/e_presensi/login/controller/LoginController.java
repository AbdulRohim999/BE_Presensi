package com.example.e_presensi.login.controller;

import com.example.e_presensi.login.dto.UserLoginRequest;
import com.example.e_presensi.login.dto.UserLoginResponse;
import com.example.e_presensi.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API untuk autentikasi pengguna")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Login pengguna", description = "Endpoint untuk login pengguna dengan email dan password")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        logger.info("Menerima permintaan login untuk email: {}", request.getEmail());
        
        // Validasi request
        if (request == null || request.getEmail() == null || request.getEmail().isEmpty() || 
            request.getPassword() == null || request.getPassword().isEmpty()) {
            logger.warn("Request login tidak valid: data tidak lengkap");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Data login tidak lengkap"));
        }
        
        try {
            UserLoginResponse response = loginService.login(request);
            
            if (response != null) {
                logger.info("Login berhasil untuk email: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Login gagal untuk email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Email atau password salah"));
            }
        } catch (Exception e) {
            logger.error("Error saat proses login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Terjadi kesalahan internal: " + e.getMessage()));
        }
    }

    // Kelas untuk response error
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}