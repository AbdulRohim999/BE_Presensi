package com.example.e_presensi.admin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.e_presensi.util.DateTimeUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "informasi")
public class Informasi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "informasi_id")
    private Integer informasiId;
    
    @Column(name = "judul", nullable = false)
    private String judul;
    
    @Column(name = "keterangan", nullable = false, columnDefinition = "TEXT")
    private String keterangan;
    
    @Column(name = "kategori", nullable = false)
    private String kategori;
    
    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;
    
    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "target_tipeuser", nullable = false)
    private String targetTipeUser; // "dosen", "karyawan", "semua"
    
    @PrePersist
    protected void onCreate() {
        createdAt = DateTimeUtil.getCurrentDateTimeWIB();
    }
} 