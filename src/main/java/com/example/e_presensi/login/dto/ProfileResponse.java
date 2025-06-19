package com.example.e_presensi.login.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Integer idUser;
    private String firstname;
    private String lastname;
    private String tempatTanggalLahir;
    private String email;
    private String role;
    private String nip;
    private String tipeUser;
    private String status;
    private String bidangKerja;
    private String alamat;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}