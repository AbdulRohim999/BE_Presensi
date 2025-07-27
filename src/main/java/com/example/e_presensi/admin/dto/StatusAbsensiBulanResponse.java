package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusAbsensiBulanResponse {
    private Integer idUser;
    private String namaUser;
    private String bidangKerja;
    private String periode;
    private Integer valid;
    private Integer invalid;
    private Integer total;
} 