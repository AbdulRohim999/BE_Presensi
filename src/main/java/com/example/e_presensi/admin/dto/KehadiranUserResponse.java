package com.example.e_presensi.admin.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KehadiranUserResponse {
    private Integer idUser;
    private String namaUser;
    private String bidangKerja;
    private String role; // Field role yang ditambahkan
    private LocalDate tanggalAbsensi;
    private LocalTime absenPagi;
    private LocalTime absenSiang;
    private LocalTime absenSore;
    private String status;
}
