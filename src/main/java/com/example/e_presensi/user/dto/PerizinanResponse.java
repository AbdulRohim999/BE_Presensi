package com.example.e_presensi.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerizinanResponse {
    private Integer idPerizinan;
    private Integer idUser;
    private String jenisIzin;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String keterangan;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String namaUser;
}