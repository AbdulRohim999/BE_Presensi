package com.example.e_presensi.admin.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.admin.dto.KehadiranUserResponse;
import com.example.e_presensi.admin.dto.StatusAbsensiBulanResponse;
import com.example.e_presensi.admin.dto.UserAbsensiStatusResponse;
import com.example.e_presensi.admin.service.KehadiranService;
import com.example.e_presensi.user.dto.LaporanKehadiranUserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/kehadiran")
@PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
@Tag(name = "Admin Kehadiran", description = "API untuk manajemen kehadiran oleh admin")
public class AdminKehadiranController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminKehadiranController.class);
    
    @Autowired
    private KehadiranService kehadiranService;
    
    @GetMapping("/hari-ini")
    @Operation(summary = "Mendapatkan kehadiran user hari ini", 
               description = "Endpoint untuk mendapatkan data kehadiran user hari ini")
    public ResponseEntity<?> getKehadiranUserHariIni() {
        try {
            List<KehadiranUserResponse> kehadiranList = kehadiranService.getKehadiranUserHariIni();
            
            if (kehadiranList.isEmpty()) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran hari ini");
                return ResponseEntity.ok(info);
            }
            
            logger.info("Berhasil mendapatkan {} data kehadiran user hari ini", kehadiranList.size());
            return ResponseEntity.ok(kehadiranList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan data kehadiran user hari ini", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan data kehadiran user hari ini");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user/{id}")
    @Operation(summary = "Mendapatkan seluruh data kehadiran user berdasarkan ID", 
               description = "Endpoint untuk mendapatkan seluruh data kehadiran user berdasarkan ID user")
    public ResponseEntity<?> getKehadiranByUserId(
            @Parameter(description = "ID user", required = true) 
            @PathVariable("id") Integer idUser) {
        
        try {
            List<KehadiranUserResponse> kehadiranList = kehadiranService.getKehadiranByUserId(idUser);
            
            if (kehadiranList.isEmpty()) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran untuk user dengan ID " + idUser);
                return ResponseEntity.ok(info);
            }
            
            logger.info("Berhasil mendapatkan {} data kehadiran untuk user dengan ID {}", kehadiranList.size(), idUser);
            return ResponseEntity.ok(kehadiranList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan data kehadiran user dengan ID {}", idUser, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan data kehadiran user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/laporan/user/minggu")
    @Operation(summary = "Mendapatkan laporan kehadiran user berdasarkan minggu", 
               description = "Endpoint untuk mendapatkan laporan kehadiran user berdasarkan minggu")
    public ResponseEntity<?> getLaporanKehadiranUserByWeek(
            @Parameter(description = "Nomor minggu (1-5)", required = true) 
            @RequestParam("week") Integer week,
            @Parameter(description = "Bulan (1-12)", required = true) 
            @RequestParam("month") Integer month,
            @Parameter(description = "Tahun", required = true) 
            @RequestParam("year") Integer year) {
        
        try {
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
            
            List<LaporanKehadiranUserResponse> laporanList = kehadiranService.getLaporanKehadiranUserByWeek(week, month, year);
            
            if (laporanList.isEmpty()) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran untuk minggu ke-" + week + " bulan " + month + " tahun " + year);
                return ResponseEntity.ok(info);
            }
            
            logger.info("Berhasil mendapatkan {} data laporan kehadiran user untuk minggu ke-{} bulan {} tahun {}", 
                    laporanList.size(), week, month, year);
            return ResponseEntity.ok(laporanList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan laporan kehadiran user untuk minggu ke-{} bulan {} tahun {}", 
                    week, month, year, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan laporan kehadiran user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/laporan/user/bulan")
    @Operation(summary = "Mendapatkan laporan kehadiran user berdasarkan bulan", 
               description = "Endpoint untuk mendapatkan laporan kehadiran user berdasarkan bulan")
    public ResponseEntity<?> getLaporanKehadiranUserByMonth(
            @Parameter(description = "Bulan (1-12)", required = true) 
            @RequestParam("month") Integer month,
            @Parameter(description = "Tahun", required = true) 
            @RequestParam("year") Integer year) {
        
        try {
            // Validasi parameter
            if (month < 1 || month > 12) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Bulan harus antara 1-12");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<LaporanKehadiranUserResponse> laporanList = kehadiranService.getLaporanKehadiranUserByMonth(month, year);
            
            if (laporanList.isEmpty()) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Belum ada data kehadiran untuk bulan " + month + " tahun " + year);
                return ResponseEntity.ok(info);
            }
            
            logger.info("Berhasil mendapatkan {} data laporan kehadiran user untuk bulan {} tahun {}", 
                    laporanList.size(), month, year);
            return ResponseEntity.ok(laporanList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan laporan kehadiran user untuk bulan {} tahun {}", 
                    month, year, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan laporan kehadiran user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/jumlah-status")
    @Operation(summary = "Mendapatkan jumlah absensi berdasarkan status untuk semua user", 
               description = "Endpoint untuk mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk semua user")
    public ResponseEntity<?> getJumlahAbsensiByStatus() {
        try {
            List<UserAbsensiStatusResponse> jumlahAbsensiAllUsers = kehadiranService.getJumlahAbsensiByStatusForAllUsers();
            
            logger.info("Berhasil mendapatkan jumlah absensi berdasarkan status untuk semua user");
            return ResponseEntity.ok(jumlahAbsensiAllUsers);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan jumlah absensi berdasarkan status untuk semua user", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan jumlah absensi berdasarkan status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/jumlah-status/periode")
    @Operation(summary = "Mendapatkan jumlah absensi berdasarkan status untuk semua user dalam periode tertentu", 
               description = "Endpoint untuk mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk semua user dalam periode tertentu")
    public ResponseEntity<?> getJumlahAbsensiByStatusAndPeriode(
            @Parameter(description = "Tanggal mulai (format: yyyy-MM-dd)", required = true) 
            @RequestParam("startDate") String startDateStr,
            @Parameter(description = "Tanggal selesai (format: yyyy-MM-dd)", required = true) 
            @RequestParam("endDate") String endDateStr) {
        
        try {
            // Parsing tanggal dengan penanganan format yang lebih baik
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            
            // Validasi tanggal
            if (endDate.isBefore(startDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tanggal selesai tidak boleh sebelum tanggal mulai");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<UserAbsensiStatusResponse> jumlahAbsensiAllUsers = kehadiranService.getJumlahAbsensiByStatusForAllUsersAndPeriode(startDate, endDate);
            
            logger.info("Berhasil mendapatkan jumlah absensi berdasarkan status untuk semua user dalam periode {} sampai {}", 
                    startDateStr, endDateStr);
            return ResponseEntity.ok(jumlahAbsensiAllUsers);
        } catch (DateTimeParseException e) {
            logger.error("Error format tanggal tidak valid", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Format tanggal tidak valid. Gunakan format yyyy-MM-dd");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan jumlah absensi berdasarkan status untuk semua user dalam periode tertentu", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan jumlah absensi berdasarkan status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/update-status")
    @Operation(summary = "Memperbarui status absensi yang tidak konsisten", 
               description = "Endpoint untuk memperbarui status absensi yang mungkin tidak konsisten di database")
    public ResponseEntity<?> updateAbsensiStatus(
            @Parameter(description = "Tanggal mulai (format: yyyy-MM-dd)", required = true) 
            @RequestParam("startDate") String startDateStr,
            @Parameter(description = "Tanggal selesai (format: yyyy-MM-dd)", required = true) 
            @RequestParam("endDate") String endDateStr) {
        
        try {
            // Parsing tanggal
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            
            // Validasi tanggal
            if (endDate.isBefore(startDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tanggal selesai tidak boleh sebelum tanggal mulai");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Update status absensi
            kehadiranService.updateAbsensiStatusForPeriod(startDate, endDate);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Status absensi berhasil diperbarui untuk periode " + startDateStr + " sampai " + endDateStr);
            
            logger.info("Status absensi berhasil diperbarui untuk periode {} sampai {}", startDateStr, endDateStr);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            logger.error("Error format tanggal tidak valid", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Format tanggal tidak valid. Gunakan format yyyy-MM-dd");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat memperbarui status absensi", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat memperbarui status absensi");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/berdasarkan-status/bulan")
    @Operation(summary = "Mendapatkan data absensi berdasarkan status di bulan tertentu", 
               description = "Endpoint untuk mendapatkan data absensi berdasarkan status (Valid, Invalid, Pending) di bulan yang ditentukan")
    public ResponseEntity<?> getAbsensiByStatusAndMonth(
            @Parameter(description = "Status absensi (Valid, Invalid, Pending)", required = true) 
            @RequestParam("status") String status,
            @Parameter(description = "Bulan (1-12)", required = true) 
            @RequestParam("month") Integer month,
            @Parameter(description = "Tahun", required = true) 
            @RequestParam("year") Integer year) {
        
        try {
            // Validasi parameter
            if (month < 1 || month > 12) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Bulan harus antara 1-12");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (year < 2020 || year > 2030) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tahun harus antara 2020-2030");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validasi status
            String statusLower = status.toLowerCase();
            if (!statusLower.equals("valid") && !statusLower.equals("invalid") && !statusLower.equals("pending")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Status harus Valid, Invalid, atau Pending");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<StatusAbsensiBulanResponse> absensiList = kehadiranService.getAbsensiByStatusAndMonth(status, month, year);
            
            if (absensiList.isEmpty()) {
                Map<String, String> info = new HashMap<>();
                info.put("message", "Tidak ada data absensi untuk bulan " + month + " tahun " + year);
                return ResponseEntity.ok(info);
            }
            
            logger.info("Berhasil mendapatkan {} data absensi untuk bulan {} tahun {}", 
                    absensiList.size(), month, year);
            return ResponseEntity.ok(absensiList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan data absensi berdasarkan status '{}' untuk bulan {} tahun {}", 
                    status, month, year, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan data absensi berdasarkan status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}