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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

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
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mengajukan perizinan", 
               description = "Endpoint untuk mengajukan perizinan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perizinan berhasil diajukan"),
        @ApiResponse(responseCode = "400", description = "Data tidak valid"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> ajukanPerizinan(
            @Parameter(description = "Data perizinan", required = true)
            @RequestPart("data") PerizinanRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            // Dapatkan ID user dari request
            Integer idUser = getUserIdFromRequest(httpRequest);
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            // Ajukan perizinan
            PerizinanResponse response = perizinanService.createPerizinan(idUser, request);
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
               description = "Endpoint untuk menghapus perizinan")
    public ResponseEntity<?> deletePerizinan(
            @Parameter(description = "ID perizinan", required = true)
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(httpRequest);
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            perizinanService.deletePerizinan(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Perizinan berhasil dihapus");
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
        if (userId == null) {
            logger.error("User ID tidak ditemukan dalam request");
            return null;
        }
        return (Integer) userId;
    }
    
    @GetMapping("/riwayat")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan riwayat perizinan", 
               description = "Endpoint untuk mendapatkan riwayat perizinan user")
    public ResponseEntity<?> getRiwayatIzin(HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(request);
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            List<PerizinanResponse> riwayatList = perizinanService.getRiwayatIzin(idUser);
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
               description = "Endpoint untuk mendapatkan daftar perizinan berdasarkan status")
    public ResponseEntity<?> getPerizinanByStatus(
            @PathVariable("status") String status, 
            HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: User tidak terautentikasi"));
            }
            
            Integer idUser = getUserIdFromRequest(request);
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak: ID user tidak valid"));
            }
            
            List<PerizinanResponse> perizinanList = perizinanService.getAllPerizinanByStatus(status);
            return ResponseEntity.ok(perizinanList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan perizinan berdasarkan status", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan perizinan berdasarkan status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}