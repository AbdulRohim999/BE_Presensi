package com.example.e_presensi.admin.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformasiRequest {
    
    @NotBlank(message = "Judul informasi tidak boleh kosong")
    private String judul;
    
    @NotBlank(message = "Keterangan informasi tidak boleh kosong")
    private String keterangan;
    
    @NotBlank(message = "Kategori tidak boleh kosong")
    private String kategori;
    
    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    private LocalDate tanggalMulai;
    
    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    private LocalDate tanggalSelesai;
    
    @NotBlank(message = "Target tipe user tidak boleh kosong")
    private String targetTipeUser; // "dosen", "karyawan", "semua"
} 