package com.example.e_presensi.admin.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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
    private String tanggalLaporan; // Format: "15 Juli 2024"
    private String hariLaporan; // Format: "Senin", "Selasa", dll
    private Integer tepatWaktu; // Jumlah kehadiran tepat waktu (status "Valid")
    private Integer terlambat; // Jumlah kehadiran terlambat (status "Invalid")
    private Integer tidakMasuk; // Jumlah tidak masuk (status "Belum Lengkap" dan tanggal sudah lewat)
    private Integer izin; // Jumlah izin (jika ada fitur izin)
    
    // Method untuk mengisi tanggalLaporan dan hariLaporan
    public void setTanggalLaporan(LocalDate tanggal) {
        if (tanggal != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            this.tanggalLaporan = tanggal.format(formatter);
            this.hariLaporan = tanggal.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
        }
    }
}