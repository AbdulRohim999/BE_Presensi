package com.example.e_presensi.user.dto;

import lombok.Data;

@Data
public class AbsensiRequest {
    private String tipeAbsen; // "pagi", "siang", atau "sore"
}