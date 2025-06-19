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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.admin.dto.UserCreateRequest;
import com.example.e_presensi.admin.dto.UserResponse;
import com.example.e_presensi.admin.dto.UserUpdateRequest;
import com.example.e_presensi.admin.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyAuthority('admin', 'super_admin')")
@Tag(name = "Admin User Management", description = "API untuk manajemen pengguna oleh admin")
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private UserManagementService userManagementService;
    
    @GetMapping
    @Operation(summary = "Mendapatkan daftar pengguna", 
               description = "Endpoint untuk mendapatkan daftar pengguna dengan role user saja")
    public ResponseEntity<?> getAllUsers() {
        
        try {
            // Menggunakan "user" dengan huruf kecil semua sesuai dengan database
            List<UserResponse> users = userManagementService.getUsersByRole("user");
            
            logger.info("Jumlah pengguna dengan role user: {}", users.size());
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan daftar pengguna", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan daftar pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Mendapatkan detail pengguna", 
               description = "Endpoint untuk mendapatkan detail pengguna berdasarkan ID")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID pengguna", required = true) 
            @PathVariable("id") Integer idUser) {
        
        try {
            UserResponse user = userManagementService.getUserById(idUser);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan detail pengguna", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan detail pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Membuat pengguna baru", 
               description = "Endpoint untuk membuat pengguna baru")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        try {
            UserResponse createdUser = userManagementService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat membuat pengguna baru", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat membuat pengguna baru", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat membuat pengguna baru");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mengubah data pengguna", 
               description = "Endpoint untuk mengubah data pengguna berdasarkan ID")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID pengguna", required = true) 
            @PathVariable("id") Integer idUser,
            @RequestBody UserUpdateRequest request) {
        
        try {
            UserResponse updatedUser = userManagementService.updateUser(idUser, request);
            
            if (updatedUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            logger.info("Berhasil mengubah data pengguna dengan ID: {}", idUser);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error validasi saat mengubah data pengguna", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error saat mengubah data pengguna", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mengubah data pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('super_admin')")
    @Operation(summary = "Menghapus data pengguna", 
               description = "Endpoint untuk menghapus data pengguna berdasarkan ID")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID pengguna", required = true) 
            @PathVariable("id") Integer idUser) {
        
        try {
            boolean isDeleted = userManagementService.deleteUser(idUser);
            
            if (!isDeleted) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Pengguna tidak ditemukan");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            logger.info("Berhasil menghapus data pengguna dengan ID: {}", idUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Pengguna berhasil dihapus");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saat menghapus data pengguna", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat menghapus data pengguna");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/total/tipe-user/{tipeUser}")
    @Operation(summary = "Mendapatkan total user berdasarkan tipe_user", 
               description = "Endpoint untuk mendapatkan total user berdasarkan tipe_user (Dosen atau Karyawan)")
    public ResponseEntity<?> getTotalUserByTipeUser(
            @Parameter(description = "Tipe user (Dosen atau Karyawan)", required = true) 
            @PathVariable("tipeUser") String tipeUser) {
        
        try {
            // Validasi tipe_user
            if (!tipeUser.equals("Dosen") && !tipeUser.equals("Karyawan")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tipe user hanya boleh: Dosen atau Karyawan");
                return ResponseEntity.badRequest().body(error);
            }
            
            long total = userManagementService.getTotalUserByTipeUser(tipeUser);
            
            Map<String, Long> response = new HashMap<>();
            response.put("total", total);
            
            logger.info("Total user dengan tipe_user {}: {}", tipeUser, total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan total user berdasarkan tipe_user", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan total user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/total/tipe-user")
    @Operation(summary = "Mendapatkan total user untuk semua tipe_user", 
               description = "Endpoint untuk mendapatkan total user untuk semua tipe_user (Dosen dan Karyawan)")
    public ResponseEntity<?> getTotalUserByAllTipeUser() {
        try {
            Map<String, Long> totals = userManagementService.getTotalUserByAllTipeUser();
            
            logger.info("Total user berdasarkan tipe_user berhasil diambil");
            
            return ResponseEntity.ok(totals);
        } catch (Exception e) {
            logger.error("Error saat mendapatkan total user untuk semua tipe_user", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan total user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}