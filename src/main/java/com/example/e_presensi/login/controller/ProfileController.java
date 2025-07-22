package com.example.e_presensi.login.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_presensi.login.dto.PasswordChangeRequest;
import com.example.e_presensi.login.dto.ProfilePhotoResponse;
import com.example.e_presensi.login.dto.ProfileRequest;
import com.example.e_presensi.login.dto.ProfileResponse;
import com.example.e_presensi.login.service.ProfilePhotoService;
import com.example.e_presensi.login.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/user/profile")
@Tag(name = "User Profile", description = "API untuk manajemen profil pengguna")
public class ProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private ProfilePhotoService profilePhotoService;
    
    // Helper method untuk mendapatkan ID user dari request
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new AccessDeniedException("User ID tidak ditemukan dalam request");
        }
        return (Integer) userId;
    }
    
    @GetMapping
    @Operation(summary = "Mendapatkan profil pengguna", 
               description = "Endpoint untuk mendapatkan data profil pengguna yang sedang login")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        try {
            Integer idUser = getUserIdFromRequest(request);
            ProfileResponse profile = profileService.getUserProfile(idUser);
            
            if (profile == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profil pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(profile);
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat mendapatkan profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan profil pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping
    @Operation(summary = "Memperbarui profil pengguna", 
               description = "Endpoint untuk memperbarui data profil pengguna yang sedang login")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody ProfileRequest profileRequest,
            HttpServletRequest request) {
        
        try {
            // Log untuk debugging
            logger.info("Mencoba memperbarui profil pengguna");
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            logger.info("Principal: {}", principal);
            logger.info("Authorities: {}", authorities);
            
            Integer idUser = getUserIdFromRequest(request);
            logger.info("ID User dari request: {}", idUser);
            
            ProfileResponse updatedProfile = profileService.updateUserProfile(idUser, profileRequest);
            
            if (updatedProfile == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profil pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(updatedProfile);
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat memperbarui profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat memperbarui profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat memperbarui profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat memperbarui profil pengguna: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/photo")
    @Operation(summary = "Mendapatkan foto profil pengguna", 
               description = "Endpoint untuk mendapatkan foto profil pengguna yang sedang login")
    public ResponseEntity<?> getProfilePhoto(HttpServletRequest request) {
        try {
            Integer idUser = getUserIdFromRequest(request);
            ProfilePhotoResponse photo = profilePhotoService.getProfilePhoto(idUser);
            
            if (photo == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Foto profil pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(photo);
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat mendapatkan foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan foto profil pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping(value = "/photo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
    @Operation(summary = "Memperbarui foto profil pengguna", 
               description = "Endpoint untuk memperbarui foto profil pengguna yang sedang login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Foto profil berhasil diperbarui", 
                    content = @Content(schema = @Schema(implementation = ProfilePhotoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Format file tidak valid atau ukuran file terlalu besar"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak"),
        @ApiResponse(responseCode = "404", description = "Profil pengguna tidak ditemukan"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> updateProfilePhoto(
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request) {
        try {
            Integer idUser = getUserIdFromRequest(request);
            ProfilePhotoResponse updatedPhoto = profilePhotoService.uploadProfilePhoto(idUser, file);
            if (updatedPhoto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Profil pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(updatedPhoto);
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat memperbarui foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Error saat memperbarui foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat memperbarui foto profil pengguna: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    
    @DeleteMapping("/photo")
    @Operation(summary = "Menghapus foto profil pengguna", 
               description = "Endpoint untuk menghapus foto profil pengguna yang sedang login")
    public ResponseEntity<?> deleteProfilePhoto(HttpServletRequest request) {
        try {
            Integer idUser = getUserIdFromRequest(request);
            profilePhotoService.deleteProfilePhoto(idUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Foto profil berhasil dihapus");
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat menghapus foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Error saat menghapus foto profil", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat menghapus foto profil pengguna: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Mengganti password pengguna", 
               description = "Endpoint untuk mengganti password pengguna yang sedang login (role: user, admin, super admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password berhasil diubah"),
        @ApiResponse(responseCode = "400", description = "Validasi gagal atau password lama tidak cocok"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak"),
        @ApiResponse(responseCode = "404", description = "Profil pengguna tidak ditemukan"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Validasi request
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty() ||
                request.getNewPassword() == null || request.getNewPassword().isEmpty() ||
                request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty()) {
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Semua field harus diisi");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validasi konfirmasi password
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password baru dan konfirmasi password tidak cocok");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validasi kekuatan password
            if (request.getNewPassword().length() < 8) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password baru harus minimal 8 karakter");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Dapatkan ID user dari request
            Integer idUser = getUserIdFromRequest(httpRequest);
            logger.info("Mencoba mengganti password untuk user ID: {}", idUser);
            
            // Panggil service untuk mengganti password
            boolean success = profileService.changePassword(idUser, request.getOldPassword(), request.getNewPassword());
            
            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password berhasil diubah");
                return ResponseEntity.ok().body(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password lama tidak cocok");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (AccessDeniedException e) {
            logger.error("Error otorisasi saat mengganti password", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Akses ditolak: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Error saat mengganti password", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Terjadi kesalahan saat mengganti password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}