package com.example.e_presensi.user.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "perizinan")
public class Perizinan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perizinan")
    private Integer idPerizinan;
    
    @Column(name = "id_user", nullable = false)
    private Integer idUser;
    
    @Column(name = "jenis_izin", nullable = false)
    private String jenisIzin;
    
    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;
    
    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;
    
    @Column(name = "keterangan", nullable = false)
    private String keterangan;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
    
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;
    
    @Column(name = "lampiran")
    private String lampiran;
    
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }
}