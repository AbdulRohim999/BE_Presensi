package com.example.e_presensi.user.dto;

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
public class PerizinanRequest {
    @NotBlank(message = "Jenis perizinan tidak boleh kosong")
    private String jenisIzin;
    
    @NotBlank(message = "Keterangan tidak boleh kosong")
    private String keterangan;
    
    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    private String tanggalMulai;
    
    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    private String tanggalSelesai;

    // private MultipartFile lampiran;
}