package com.example.e_presensi.login.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer id_user;
    
    @Column(name = "firstname")
    private String firstname;
    
    @Column(name = "lastname")
    private String lastname;

    @Column(name = "tempat_tanggal_lahir")
    private String tempatTanggalLahir;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "alamat")
    private String alamat;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "nip")
    private String nip;
    
    @Column(name = "tipe_user")
    private String tipeUser;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "foto_profile")
    private String fotoProfile;
    
    @Column(name = "bidang_kerja")
    private String bidangKerja;
    
    @Column(name = "create_at")
    private LocalDateTime createAt;
    
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    
    @OneToOne(mappedBy = "userProfile")
    private Login login;
}