package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanKehadiranUserResponse {
    private Integer idUser;
    private String namaUser;
    private String bidangKerja;
    private String periode; // Minggu ke-X atau Bulan X
    private Integer valid; // Jumlah kehadiran tepat waktu (status "Valid")
    private Integer invalid; // Jumlah kehadiran terlambat (status "Invalid")
    private Integer total; // Total absensi
}