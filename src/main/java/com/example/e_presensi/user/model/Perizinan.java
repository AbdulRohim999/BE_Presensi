package com.example.e_presensi.user.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.e_presensi.login.model.UserProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private UserProfile userProfile;
    
    @Column(nullable = false)
    private String jenisIzin;
    
    @Column(nullable = false)
    private String keterangan;
    
    @Column(nullable = false)
    private LocalDate tanggalMulai;
    
    @Column(nullable = false)
    private LocalDate tanggalSelesai;
    
    @Column(nullable = false)
    private String status;
    
    // Menghapus field lampiran
    // @Column
    // private String lampiran;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}