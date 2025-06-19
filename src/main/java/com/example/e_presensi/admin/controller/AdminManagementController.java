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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.admin.dto.AdminCreateRequest;
import com.example.e_presensi.admin.dto.AdminUpdateRequest;
import com.example.e_presensi.admin.dto.UserResponse;
import com.example.e_presensi.admin.service.AdminManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/superadmin/admins")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('super_admin')")
@Tag(name = "SuperAdmin Admin Management", description = "API untuk manajemen admin oleh super admin")
public class AdminManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementController.class);
    
    @Autowired
    private AdminManagementService adminManagementService;
    
    @GetMapping
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Mendapatkan daftar admin", 
               description = "Endpoint untuk mendapatkan daftar semua admin")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<UserResponse> admins = adminManagementService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan daftar admin: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan daftar admin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Mendapatkan detail admin", 
               description = "Endpoint untuk mendapatkan detail admin berdasarkan ID")
    public ResponseEntity<?> getAdminById(
            @Parameter(description = "ID admin", required = true) 
            @PathVariable("id") Integer idAdmin) {
        
        try {
            UserResponse admin = adminManagementService.getAdminById(idAdmin);
            
            if (admin == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Admin tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan detail admin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan detail admin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Membuat admin baru", 
               description = "Endpoint untuk membuat admin baru oleh super admin")
    public ResponseEntity<?> createAdmin(@RequestBody AdminCreateRequest request) {
        try {
            // Pastikan role admin menggunakan huruf kecil
            UserResponse createdAdmin = adminManagementService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAdmin);
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat membuat admin baru", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat membuat admin baru", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat membuat admin baru");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Mendapatkan jumlah admin", 
               description = "Endpoint untuk mendapatkan jumlah admin yang terdaftar dalam sistem")
    public ResponseEntity<?> getAdminCount() {
        try {
            long count = adminManagementService.getAdminCount();
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            logger.info("Jumlah admin: {}", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan jumlah admin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan jumlah admin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Mengubah data admin", 
               description = "Endpoint untuk mengubah data admin berdasarkan ID")
    public ResponseEntity<?> updateAdmin(
            @Parameter(description = "ID admin", required = true) 
            @PathVariable("id") Integer idAdmin,
            @RequestBody AdminUpdateRequest request) {
        
        try {
            UserResponse updatedAdmin = adminManagementService.updateAdmin(idAdmin, request);
            
            if (updatedAdmin == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Admin tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(updatedAdmin);
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat mengubah admin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat mengubah admin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mengubah admin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "Menghapus admin", 
               description = "Endpoint untuk menghapus admin berdasarkan ID")
    public ResponseEntity<?> deleteAdmin(
            @Parameter(description = "ID admin", required = true) 
            @PathVariable("id") Integer idAdmin) {
        
        try {
            boolean deleted = adminManagementService.deleteAdmin(idAdmin);
            
            if (!deleted) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Admin tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin berhasil dihapus");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saat menghapus admin", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat menghapus admin");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}