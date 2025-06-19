package com.example.e_presensi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsensiResponse {
    private Integer idAbsensi;
    private LocalDate tanggal;
    private LocalDateTime absenPagi;
    private String statusPagi;
    private LocalDateTime absenSiang;
    private String statusSiang;
    private LocalDateTime absenSore;
    private String statusSore;
    private String status;
}