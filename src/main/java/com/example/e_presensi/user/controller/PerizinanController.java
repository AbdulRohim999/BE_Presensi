package com.example.e_presensi.user.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_presensi.user.dto.PerizinanRequest;
import com.example.e_presensi.user.dto.PerizinanResponse;
import com.example.e_presensi.user.service.PerizinanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/perizinan")
@Tag(name = "Perizinan", description = "API untuk manajemen perizinan pengguna")
@SecurityRequirement(name = "bearerAuth")
public class PerizinanController {

    private static final Logger logger = LoggerFactory.getLogger(PerizinanController.class);

    @Autowired
    private PerizinanService perizinanService;
    
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mengajukan perizinan dengan lampiran", description = "Endpoint untuk mengajukan perizinan beserta file lampiran (PDF, Word, gambar)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perizinan berhasil diajukan"),
        @ApiResponse(responseCode = "400", description = "Data tidak valid"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk user"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> ajukanPerizinan(
        @RequestParam("jenisIzin") String jenisIzin,
        @RequestParam("keterangan") String keterangan,
        @RequestParam("tanggalMulai") String tanggalMulai,
        @RequestParam("tanggalSelesai") String tanggalSelesai,
        @RequestParam(value = "lampiran", required = false) MultipartFile lampiran,
        HttpServletRequest httpRequest) {
        PerizinanRequest request = PerizinanRequest.builder()
            .jenisIzin(jenisIzin)
            .keterangan(keterangan)
            .tanggalMulai(tanggalMulai)
            .tanggalSelesai(tanggalSelesai)
            .build();
        
        logger.info("=== MULAI: Mengajukan perizinan ===");
        
        try {
            // Debug authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Authentication object: {}", auth);
            
            if (auth == null) {
                logger.error("Authentication object is null");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: Authentication object is null"));
            }
            
            if (!auth.isAuthenticated()) {
                logger.error("User tidak terautentikasi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            // Log authorities
            logger.info("Authorities: {}", auth.getAuthorities());
            logger.info("Principal: {}", auth.getPrincipal());
            
            // Dapatkan ID user dari request
            Integer idUser = getUserIdFromRequest(httpRequest);
            logger.info("ID User dari request: {}", idUser);
            
            if (idUser == null) {
                logger.error("ID user tidak valid - userId attribute is null");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            // Validasi request
            if (request == null) {
                logger.error("Request data is null");
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Data perizinan tidak boleh kosong"));
            }
            
            logger.info("Data perizinan: jenis={}, keterangan={}, tanggalMulai={}, tanggalSelesai={}", 
                    request.getJenisIzin(), request.getKeterangan(), 
                    request.getTanggalMulai(), request.getTanggalSelesai());
            
            // Ajukan perizinan
            PerizinanResponse response = perizinanService.createPerizinanWithLampiran(idUser, request, lampiran);
            logger.info("Perizinan berhasil diajukan untuk user ID: {}", idUser);
            
            logger.info("=== SELESAI: Mengajukan perizinan ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat mengajukan perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat mengajukan perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mengajukan perizinan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Menghapus perizinan", 
               description = "Endpoint untuk menghapus perizinan (hanya untuk user)")
    public ResponseEntity<?> deletePerizinan(
            @Parameter(description = "ID perizinan", required = true)
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        
        logger.info("=== MULAI: Menghapus perizinan dengan ID: {} ===", id);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warn("User tidak terautentikasi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(httpRequest);
            if (idUser == null) {
                logger.error("ID user tidak valid");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            perizinanService.deletePerizinan(id);
            logger.info("Perizinan berhasil dihapus untuk user ID: {}", idUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Perizinan berhasil dihapus");
            
            logger.info("=== SELESAI: Menghapus perizinan ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error saat menghapus perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat menghapus perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat menghapus perizinan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Helper method untuk mendapatkan ID user dari request
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        logger.info("Request attribute 'userId': {}", userId);
        
        if (userId == null) {
            logger.error("User ID tidak ditemukan dalam request");
            return null;
        }
        
        if (userId instanceof Integer) {
            logger.info("User ID ditemukan: {}", userId);
            return (Integer) userId;
        } else {
            logger.error("User ID bukan Integer: {} (type: {})", userId, userId.getClass().getName());
            return null;
        }
    }
    
    @GetMapping("/riwayat")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan riwayat perizinan", 
               description = "Endpoint untuk mendapatkan riwayat perizinan user (hanya untuk user)")
    public ResponseEntity<?> getRiwayatIzin(HttpServletRequest request) {
        logger.info("=== MULAI: Mendapatkan riwayat perizinan ===");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warn("User tidak terautentikasi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(request);
            if (idUser == null) {
                logger.error("ID user tidak valid");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            List<PerizinanResponse> riwayatList = perizinanService.getRiwayatIzin(idUser);
            logger.info("Berhasil mendapatkan {} riwayat perizinan untuk user ID: {}", riwayatList.size(), idUser);
            
            logger.info("=== SELESAI: Mendapatkan riwayat perizinan ===");
            return ResponseEntity.ok(riwayatList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan riwayat izin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan riwayat izin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan perizinan berdasarkan status", 
               description = "Endpoint untuk mendapatkan daftar perizinan berdasarkan status (hanya untuk user)")
    public ResponseEntity<?> getPerizinanByStatus(
            @PathVariable("status") String status, 
            HttpServletRequest request) {
        logger.info("=== MULAI: Mendapatkan perizinan dengan status: {} ===", status);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warn("User tidak terautentikasi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(request);
            if (idUser == null) {
                logger.error("ID user tidak valid");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            List<PerizinanResponse> perizinanList = perizinanService.getAllPerizinanByStatus(status);
            logger.info("Berhasil mendapatkan {} perizinan dengan status {} untuk user ID: {}", 
                       perizinanList.size(), status, idUser);
            
            logger.info("=== SELESAI: Mendapatkan perizinan berdasarkan status ===");
            return ResponseEntity.ok(perizinanList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan perizinan berdasarkan status", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan perizinan berdasarkan status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
}