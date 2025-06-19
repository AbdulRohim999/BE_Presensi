package com.example.e_presensi.user.dto;

// Hapus import yang tidak digunakan
// import org.springframework.web.multipart.MultipartFile;

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
    private String jenisPerizinan;
    
    @NotBlank(message = "Alasan tidak boleh kosong")
    private String alasan;
    
    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    private String tanggalMulai;
    
    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    private String tanggalSelesai;
}