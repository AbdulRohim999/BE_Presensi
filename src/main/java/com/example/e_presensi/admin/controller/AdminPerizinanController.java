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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.user.dto.PerizinanResponse;
import com.example.e_presensi.user.service.PerizinanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/perizinan")
@PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
@Tag(name = "Admin Perizinan", description = "API untuk manajemen perizinan oleh admin")
public class AdminPerizinanController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPerizinanController.class);

    @Autowired
    private PerizinanService perizinanService;
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Mendapatkan daftar perizinan berdasarkan status", 
               description = "Endpoint untuk mendapatkan daftar perizinan berdasarkan status (Menunggu, Diterima, Ditolak)")
    public ResponseEntity<?> getPerizinanByStatus(
            @Parameter(description = "Status perizinan", required = true) 
            @PathVariable("status") String status) {
        
        try {
            // Validasi status
            if (!status.equals("Menunggu") && !status.equals("Diterima") && !status.equals("Ditolak")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Status hanya boleh: Menunggu, Diterima, atau Ditolak");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<PerizinanResponse> perizinanList = perizinanService.getAllPerizinanByStatus(status);
            return ResponseEntity.ok(perizinanList);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan daftar perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan daftar perizinan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Mengubah status perizinan", 
               description = "Endpoint untuk mengubah status perizinan (Menunggu, Diterima, Ditolak)")
    public ResponseEntity<?> updateStatusPerizinan(
            @Parameter(description = "ID perizinan", required = true) 
            @PathVariable("id") Integer idPerizinan,
            @Parameter(description = "Status baru", required = true) 
            @RequestParam("status") String status) {
        
        try {
            // Validasi status
            if (!status.equals("Menunggu") && !status.equals("Diterima") && !status.equals("Ditolak")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Status hanya boleh: Menunggu, Diterima, atau Ditolak");
                return ResponseEntity.badRequest().body(error);
            }
            
            PerizinanResponse updatedPerizinan = perizinanService.updateStatusPerizinan(idPerizinan, status);
            if (updatedPerizinan == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Perizinan tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(updatedPerizinan);
        } catch (Exception e) {
            logger.error("Error saat mengubah status perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mengubah status perizinan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Mendapatkan detail perizinan", 
               description = "Endpoint untuk mendapatkan detail perizinan berdasarkan ID")
    public ResponseEntity<?> getDetailPerizinan(
            @Parameter(description = "ID perizinan", required = true) 
            @PathVariable("id") Integer idPerizinan) {
        
        try {
            PerizinanResponse detailPerizinan = perizinanService.getDetailIzin(idPerizinan);
            if (detailPerizinan == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Perizinan tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(detailPerizinan);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan detail perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan detail perizinan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/all")
    @Operation(summary = "Mendapatkan semua perizinan", 
               description = "Endpoint untuk mendapatkan daftar semua perizinan")
    public ResponseEntity<?> getAllPerizinan() {
        try {
            List<PerizinanResponse> allPerizinan = perizinanService.getAllPerizinan();
            return ResponseEntity.ok(allPerizinan);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan semua perizinan", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan semua perizinan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}