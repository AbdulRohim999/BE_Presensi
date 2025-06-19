package com.example.e_presensi.user.controller;

import com.example.e_presensi.login.config.JwtUtil;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.user.dto.AbsensiRequest;
import com.example.e_presensi.user.dto.AbsensiResponse;
import com.example.e_presensi.user.dto.LaporanKehadiranUserResponse;
import com.example.e_presensi.user.service.AbsensiService;
import com.example.e_presensi.config.NetworkService; // Tambahkan import ini
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/absensi")
@Tag(name = "Absensi", description = "API untuk manajemen absensi pengguna")
public class AbsensiController {

    private static final Logger logger = LoggerFactory.getLogger(AbsensiController.class);

    @Autowired
    private AbsensiService absensiService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private NetworkService networkService; // Tambahkan injeksi dependency ini
    
    @PostMapping
    @Operation(summary = "Melakukan absensi", description = "Endpoint untuk melakukan absensi (pagi, siang, atau sore)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Absensi berhasil"),
        @ApiResponse(responseCode = "400", description = "Permintaan tidak valid")
    })
    public ResponseEntity<?> melakukanAbsensi(
            @Parameter(description = "Data absensi", required = true) @RequestBody AbsensiRequest request) {
        
        // Validasi tipe absen
        if (request.getTipeAbsen() == null || request.getTipeAbsen().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tipe absen tidak boleh kosong");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validasi tipe absen hanya boleh: pagi, siang, atau sore
        String tipeAbsen = request.getTipeAbsen().toLowerCase();
        if (!tipeAbsen.equals("pagi") && !tipeAbsen.equals("siang") && !tipeAbsen.equals("sore")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tipe absen hanya boleh: pagi, siang, atau sore");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Dapatkan ID user dari request attribute (diset oleh JwtAuthFilter)
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User tidak valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        AbsensiResponse response = absensiService.melakukanAbsensi(idUser, request);
        
        if (response != null) {
            // Hanya kembalikan tipeAbsen saja
            Map<String, String> result = new HashMap<>();
            result.put("tipeAbsen", request.getTipeAbsen());
            return ResponseEntity.ok(result);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Gagal melakukan absensi");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/hari-ini")
    @Operation(summary = "Mendapatkan absensi hari ini", description = "Endpoint untuk mendapatkan data absensi hari ini")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data absensi ditemukan atau pesan jika belum ada absensi")
    })
    public ResponseEntity<?> getAbsensiHariIni() {
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User tidak valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        AbsensiResponse response = absensiService.getAbsensiHariIni(idUser);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> info = new HashMap<>();
            info.put("message", "Belum ada absensi hari ini");
            return ResponseEntity.ok(info);
        }
    }
    
    @GetMapping("/riwayat")
    @Operation(summary = "Mendapatkan riwayat absensi", description = "Endpoint untuk mendapatkan riwayat absensi pengguna")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daftar riwayat absensi")
    })
    public ResponseEntity<List<AbsensiResponse>> getRiwayatAbsensi() {
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
        }
        
        List<AbsensiResponse> responses = absensiService.getRiwayatAbsensi(idUser);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/riwayat/range")
    @Operation(summary = "Mendapatkan riwayat absensi berdasarkan rentang tanggal", 
               description = "Endpoint untuk mendapatkan riwayat absensi pengguna berdasarkan rentang tanggal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daftar riwayat absensi dalam rentang tanggal")
    })
    public ResponseEntity<List<AbsensiResponse>> getRiwayatAbsensiByDateRange(
            @Parameter(description = "Tanggal mulai (format: YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Tanggal akhir (format: YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
        }
        
        List<AbsensiResponse> responses = absensiService.getRiwayatAbsensiByDateRange(idUser, startDate, endDate);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Mendapatkan ID user dari request attribute yang diset oleh JwtAuthFilter
     */
    private Integer getUserIdFromRequest() {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr != null && userIdAttr instanceof Integer) {
            return (Integer) userIdAttr;
        }
        return null;
    }

    @GetMapping("/laporan/minggu")
    @Operation(summary = "Mendapatkan laporan kehadiran user berdasarkan minggu", 
               description = "Endpoint untuk mendapatkan laporan kehadiran user berdasarkan minggu")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Laporan kehadiran ditemukan"),
        @ApiResponse(responseCode = "404", description = "Laporan kehadiran tidak ditemukan")
    })
    public ResponseEntity<?> getLaporanKehadiranUserByWeek(
            @Parameter(description = "Nomor minggu (1-5)", required = true) 
            @RequestParam("week") Integer week,
            @Parameter(description = "Bulan (1-12)", required = true) 
            @RequestParam("month") Integer month,
            @Parameter(description = "Tahun", required = true) 
            @RequestParam("year") Integer year) {
        
        // Validasi parameter
        if (week < 1 || week > 5) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Nomor minggu harus antara 1-5");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (month < 1 || month > 12) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bulan harus antara 1-12");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Dapatkan ID user dari request attribute (diset oleh JwtAuthFilter)
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User tidak valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        try {
            LaporanKehadiranUserResponse laporan = absensiService.getLaporanKehadiranUserByWeek(idUser, week, month, year);
            
            if (laporan == null) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran untuk minggu ke-" + week + " bulan " + month + " tahun " + year);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(info);
            }
            
            logger.info("Berhasil mendapatkan laporan kehadiran user ID {} untuk minggu ke-{} bulan {} tahun {}", 
                    idUser, week, month, year);
            return ResponseEntity.ok(laporan);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan laporan kehadiran user ID {} untuk minggu ke-{} bulan {} tahun {}", 
                    idUser, week, month, year, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan laporan kehadiran");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/laporan/bulan")
    @Operation(summary = "Mendapatkan laporan kehadiran user berdasarkan bulan", 
               description = "Endpoint untuk mendapatkan laporan kehadiran user berdasarkan bulan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Laporan kehadiran ditemukan"),
        @ApiResponse(responseCode = "404", description = "Laporan kehadiran tidak ditemukan")
    })
    public ResponseEntity<?> getLaporanKehadiranUserByMonth(
            @Parameter(description = "Bulan (1-12)", required = true) 
            @RequestParam("month") Integer month,
            @Parameter(description = "Tahun", required = true) 
            @RequestParam("year") Integer year) {
        
        // Validasi parameter
        if (month < 1 || month > 12) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bulan harus antara 1-12");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Dapatkan ID user dari request attribute (diset oleh JwtAuthFilter)
        Integer idUser = getUserIdFromRequest();
        if (idUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User tidak valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        try {
            LaporanKehadiranUserResponse laporan = absensiService.getLaporanKehadiranUserByMonth(idUser, month, year);
            
            if (laporan == null) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran untuk bulan " + month + " tahun " + year);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(info);
            }
            
            logger.info("Berhasil mendapatkan laporan kehadiran user ID {} untuk bulan {} tahun {}", 
                    idUser, month, year);
            return ResponseEntity.ok(laporan);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan laporan kehadiran user ID {} untuk bulan {} tahun {}", 
                    idUser, month, year, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan laporan kehadiran");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    @GetMapping("/check-network")
    @Operation(summary = "Memeriksa apakah pengguna berada dalam jaringan kampus",
               description = "Endpoint untuk memeriksa apakah IP pengguna termasuk dalam daftar IP jaringan kampus")
    public ResponseEntity<?> checkNetworkStatus(HttpServletRequest request) {
        try {
            // Mendapatkan IP address dari request
            String clientIp = getClientIp(request);
        
            // Memeriksa apakah IP termasuk dalam range jaringan kampus
            boolean isInCampusNetwork = networkService.isInCampusNetwork(clientIp);
        
            Map<String, Object> response = new HashMap<>();
            response.put("inCampusNetwork", isInCampusNetwork);
            response.put("clientIp", clientIp);
        
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saat memeriksa status jaringan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat memeriksa status jaringan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper method untuk mendapatkan IP client
    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
