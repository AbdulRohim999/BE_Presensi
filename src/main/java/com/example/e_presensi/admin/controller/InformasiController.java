package com.example.e_presensi.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.admin.dto.InformasiRequest;
import com.example.e_presensi.admin.dto.InformasiResponse;
import com.example.e_presensi.admin.service.InformasiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Informasi", description = "API untuk manajemen informasi")
@SecurityRequirement(name = "bearerAuth")
public class InformasiController {

    private static final Logger logger = LoggerFactory.getLogger(InformasiController.class);

    @Autowired
    private InformasiService informasiService;

    // ==================== ADMIN ENDPOINTS ====================
    
    @PostMapping("/admin/informasi")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Membuat informasi baru", description = "Endpoint untuk admin membuat informasi baru")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informasi berhasil dibuat"),
        @ApiResponse(responseCode = "400", description = "Data tidak valid"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> createInformasi(
            @Valid @RequestBody InformasiRequest request) {
        
        logger.info("=== MULAI: Membuat informasi baru ===");
        
        try {
            // Dapatkan nama admin yang membuat informasi
            String createdBy = getCurrentUserName();
            logger.info("Informasi dibuat oleh: {}", createdBy);
            
            InformasiResponse response = informasiService.createInformasi(request, createdBy);
            logger.info("Informasi berhasil dibuat dengan ID: {}", response.getInformasiId());
            
            logger.info("=== SELESAI: Membuat informasi baru ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat membuat informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat membuat informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat membuat informasi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/admin/informasi")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Mendapatkan semua informasi", description = "Endpoint untuk admin melihat semua informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan semua informasi"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getAllInformasi() {
        logger.info("=== MULAI: Mendapatkan semua informasi ===");
        
        try {
            List<InformasiResponse> informasiList = informasiService.getAllInformasi();
            logger.info("Berhasil mendapatkan {} informasi", informasiList.size());
            
            logger.info("=== SELESAI: Mendapatkan semua informasi ===");
            return ResponseEntity.ok(informasiList);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan semua informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan informasi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/admin/informasi/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Mendapatkan informasi berdasarkan ID", description = "Endpoint untuk admin melihat detail informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan informasi"),
        @ApiResponse(responseCode = "404", description = "Informasi tidak ditemukan"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getInformasiById(
            @Parameter(description = "ID informasi", required = true)
            @PathVariable Integer id) {
        
        logger.info("=== MULAI: Mendapatkan informasi dengan ID: {} ===", id);
        
        try {
            InformasiResponse informasi = informasiService.getInformasiById(id);
            
            if (informasi == null) {
                logger.warn("Informasi dengan ID {} tidak ditemukan", id);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Informasi tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            logger.info("Berhasil mendapatkan informasi dengan ID: {}", id);
            logger.info("=== SELESAI: Mendapatkan informasi ===");
            return ResponseEntity.ok(informasi);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan informasi dengan ID: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan informasi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/admin/informasi/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Mengupdate informasi", description = "Endpoint untuk admin mengupdate informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informasi berhasil diupdate"),
        @ApiResponse(responseCode = "400", description = "Data tidak valid"),
        @ApiResponse(responseCode = "404", description = "Informasi tidak ditemukan"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> updateInformasi(
            @Parameter(description = "ID informasi", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody InformasiRequest request) {
        
        logger.info("=== MULAI: Mengupdate informasi dengan ID: {} ===", id);
        
        try {
            InformasiResponse response = informasiService.updateInformasi(id, request);
            logger.info("Informasi berhasil diupdate dengan ID: {}", id);
            
            logger.info("=== SELESAI: Mengupdate informasi ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat mengupdate informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat mengupdate informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mengupdate informasi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/admin/informasi/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Menghapus informasi", description = "Endpoint untuk admin menghapus informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informasi berhasil dihapus"),
        @ApiResponse(responseCode = "404", description = "Informasi tidak ditemukan"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> deleteInformasi(
            @Parameter(description = "ID informasi", required = true)
            @PathVariable Integer id) {
        
        logger.info("=== MULAI: Menghapus informasi dengan ID: {} ===", id);
        
        try {
            informasiService.deleteInformasi(id);
            logger.info("Informasi berhasil dihapus dengan ID: {}", id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Informasi berhasil dihapus");
            
            logger.info("=== SELESAI: Menghapus informasi ===");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error saat menghapus informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat menghapus informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat menghapus informasi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/admin/informasi/search")
    @PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
    @Operation(summary = "Mencari informasi berdasarkan judul", description = "Endpoint untuk admin mencari informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mencari informasi"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk admin"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> searchInformasi(
            @Parameter(description = "Kata kunci judul", required = true)
            @RequestParam String judul) {
        
        logger.info("=== MULAI: Mencari informasi dengan judul: {} ===", judul);
        
        try {
            List<InformasiResponse> informasiList = informasiService.searchInformasiByJudul(judul);
            logger.info("Berhasil menemukan {} informasi dengan judul: {}", informasiList.size(), judul);
            
            logger.info("=== SELESAI: Mencari informasi ===");
            return ResponseEntity.ok(informasiList);
            
        } catch (Exception e) {
            logger.error("Error saat mencari informasi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mencari informasi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== USER ENDPOINTS ====================
    
    @GetMapping("/user/informasi")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan informasi aktif untuk user yang login", description = "Endpoint untuk user melihat informasi aktif yang sesuai dengan tipe user mereka (dosen/karyawan) dan yang targetnya 'semua'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan informasi aktif"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk user"),
        @ApiResponse(responseCode = "401", description = "User tidak terautentikasi"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getActiveInformasiForUser() {
        logger.info("=== MULAI: Mendapatkan informasi aktif untuk user yang login ===");
        
        try {
            String username = getCurrentUserName();
            if ("Unknown User".equals(username)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User tidak terautentikasi.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<InformasiResponse> informasiList = informasiService.getActiveInformasiForUser(username);
            logger.info("Berhasil mendapatkan {} informasi aktif untuk user: {}", informasiList.size(), username);
            
            logger.info("=== SELESAI: Mendapatkan informasi aktif untuk user yang login ===");
            return ResponseEntity.ok(informasiList);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan informasi aktif untuk user", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan informasi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user/informasi/{id}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan detail informasi", description = "Endpoint untuk user melihat detail informasi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan detail informasi"),
        @ApiResponse(responseCode = "404", description = "Informasi tidak ditemukan"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk user"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getInformasiDetail(
            @Parameter(description = "ID informasi", required = true)
            @PathVariable Integer id) {
        
        logger.info("=== MULAI: Mendapatkan detail informasi dengan ID: {} ===", id);
        
        try {
            InformasiResponse informasi = informasiService.getInformasiById(id);
            
            if (informasi == null) {
                logger.warn("Informasi dengan ID {} tidak ditemukan", id);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Informasi tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            logger.info("Berhasil mendapatkan detail informasi dengan ID: {}", id);
            logger.info("=== SELESAI: Mendapatkan detail informasi ===");
            return ResponseEntity.ok(informasi);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan detail informasi dengan ID: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan detail informasi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user/informasi/tipe/{tipeUser}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan informasi aktif berdasarkan tipe user", description = "Endpoint untuk user melihat informasi yang aktif sesuai tipe usernya")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan informasi aktif"),
        @ApiResponse(responseCode = "400", description = "Tipe user tidak valid"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk user"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getActiveInformasiByTipeUser(
            @Parameter(description = "Tipe user (dosen/karyawan)", required = true)
            @PathVariable String tipeUser) {
        
        logger.info("=== MULAI: Mendapatkan informasi aktif untuk tipe user: {} ===", tipeUser);
        
        try {
            // Validasi tipe user
            String validTipeUser = tipeUser.toLowerCase().trim();
            if (!validTipeUser.equals("dosen") && !validTipeUser.equals("karyawan")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tipe user hanya boleh: dosen atau karyawan");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<InformasiResponse> informasiList = informasiService.getActiveInformasiByTipeUser(validTipeUser);
            logger.info("Berhasil mendapatkan {} informasi aktif untuk tipe user: {}", informasiList.size(), validTipeUser);
            
            logger.info("=== SELESAI: Mendapatkan informasi aktif berdasarkan tipe user ===");
            return ResponseEntity.ok(informasiList);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan informasi aktif untuk tipe user: {}", tipeUser, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan informasi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user/informasi/semua")
    @PreAuthorize("hasAuthority('user')")
    @Operation(summary = "Mendapatkan SEMUA informasi untuk user yang login", description = "Endpoint untuk user melihat semua riwayat informasi (aktif, lampau, mendatang) yang sesuai dengan tipe user mereka.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan semua informasi"),
        @ApiResponse(responseCode = "403", description = "Akses ditolak - hanya untuk user"),
        @ApiResponse(responseCode = "401", description = "User tidak terautentikasi"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getAllInformasiForUser() {
        logger.info("=== MULAI: Mendapatkan SEMUA informasi untuk user yang login ===");
        
        try {
            String username = getCurrentUserName();
            if ("Unknown User".equals(username)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User tidak terautentikasi.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<InformasiResponse> informasiList = informasiService.getAllInformasiForUser(username);
            logger.info("Berhasil mendapatkan {} total informasi untuk user: {}", informasiList.size(), username);
            
            logger.info("=== SELESAI: Mendapatkan SEMUA informasi untuk user yang login ===");
            return ResponseEntity.ok(informasiList);
            
        } catch (Exception e) {
            logger.error("Error saat mendapatkan semua informasi untuk user", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan semua informasi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Helper method untuk mendapatkan nama user yang sedang login
    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "Unknown User";
    }
} 