package com.example.e_presensi.admin.dto;

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
public class InformasiResponse {
    private Integer informasiId;
    private String judul;
    private String keterangan;
    private String kategori;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String createdBy;
    private String targetTipeUser;
    private LocalDateTime createdAt;
} 