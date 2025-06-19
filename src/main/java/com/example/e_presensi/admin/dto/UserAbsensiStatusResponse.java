package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAbsensiStatusResponse {
    private Integer idUser;
    private String namaUser;
    private String tipeUser;
    private String bidangKerja;
    private String role; // Menambahkan field role
    private Long validCount;
    private Long invalidCount;
    private Long totalCount;
}