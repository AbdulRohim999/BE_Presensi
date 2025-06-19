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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "absensi")
public class Absensi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_absensi")
    private Integer idAbsensi;
    
    @ManyToOne
    @JoinColumn(name = "id_user")
    private UserProfile userProfile;
    
    @Column(name = "tanggal")
    private LocalDate tanggal;
    
    @Column(name = "absen_pagi")
    private LocalDateTime absenPagi;
    
    @Column(name = "absen_siang")
    private LocalDateTime absenSiang;
    
    @Column(name = "absen_sore")
    private LocalDateTime absenSore;
    
    @Column(name = "status")
    private String status; // "Valid", "Invalid", atau "Belum Lengkap"
    
    @Column(name = "createat")
    private LocalDateTime createAt;
    
    @Column(name = "updateat")
    private LocalDateTime updateAt;
}