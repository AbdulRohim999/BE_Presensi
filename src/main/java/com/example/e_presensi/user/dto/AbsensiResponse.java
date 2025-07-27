package com.example.e_presensi.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsensiResponse {
    private Integer idAbsensi;
    private LocalDate tanggal;
    private String tanggalFormatted; // Format: "Senin, 15 Juli 2025"
    private String hari; // Format: "Senin", "Selasa", dll
    private LocalDateTime absenPagi;
    private String statusPagi;
    private LocalDateTime absenSiang;
    private String statusSiang;
    private LocalDateTime absenSore;
    private String statusSore;
    private String status;
    private String keteranganAbsenSore; // Keterangan khusus untuk absen sore
    
    // Method untuk mengisi tanggalFormatted dan hari
    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
        if (tanggal != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            this.tanggalFormatted = tanggal.format(formatter);
            this.hari = tanggal.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
            
            // Set keterangan absen sore berdasarkan hari
            if (tanggal.getDayOfWeek().getValue() == 6) { // Sabtu
                this.keteranganAbsenSore = "*"; // Tidak ada absen sore pada hari Sabtu
            } else if (tanggal.getDayOfWeek().getValue() == 7) { // Minggu
                this.keteranganAbsenSore = "Tidak ada absen pada hari Minggu";
            } else {
                this.keteranganAbsenSore = "Belum absen sore";
            }
        }
    }
}