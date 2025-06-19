package com.example.e_presensi.user.dto;

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
    private String role; // Field role yang ditambahkan
    private String periode; // Minggu ke-X atau Bulan X
    private Integer tepatWaktu; // Jumlah kehadiran tepat waktu (status "Valid")
    private Integer terlambat; // Jumlah kehadiran terlambat (status "Invalid")
    private Integer tidakMasuk; // Jumlah tidak masuk (status "Belum Lengkap" dan tanggal sudah lewat)
    private Integer izin; // Jumlah izin (jika ada fitur izin)
}