package com.example.e_presensi.admin.dto;

import java.time.LocalDate;
import java.time.LocalTime;
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
public class KehadiranUserResponse {
    private Integer idUser;
    private String namaUser;
    private String bidangKerja;
    private String role; // Field role yang ditambahkan
    private LocalDate tanggalAbsensi;
    private String tanggalFormatted; // Format: "Senin, 15 Juli 2025"
    private String hari; // Format: "Senin", "Selasa", dll
    private LocalTime absenPagi;
    private LocalTime absenSiang;
    private LocalTime absenSore;
    private String status;
    private String keteranganAbsenSore; // Keterangan khusus untuk absen sore
    
    // Method untuk mengisi tanggalFormatted dan hari
    public void setTanggalAbsensi(LocalDate tanggalAbsensi) {
        this.tanggalAbsensi = tanggalAbsensi;
        if (tanggalAbsensi != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            this.tanggalFormatted = tanggalAbsensi.format(formatter);
            this.hari = tanggalAbsensi.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            
            // Set keterangan absen sore berdasarkan hari
            int dayOfWeek = tanggalAbsensi.getDayOfWeek().getValue();
            if (dayOfWeek == 6) { // Sabtu
                this.keteranganAbsenSore = "*"; // Tidak ada absen sore pada hari Sabtu
            } else if (dayOfWeek == 7) { // Minggu
                this.keteranganAbsenSore = "Tidak ada absen pada hari Minggu";
            } else {
                // Senin-Jumat (1-5)
                this.keteranganAbsenSore = "Belum absen sore";
            }
        }
    }
}
