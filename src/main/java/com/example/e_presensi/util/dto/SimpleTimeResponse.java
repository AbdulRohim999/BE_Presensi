package com.example.e_presensi.util.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTimeResponse {
    
    private String date;           // Format: "dd MMMM yyyy" (contoh: "15 Desember 2024")
    private String time;           // Format: "HH:mm:ss" (contoh: "14:30:25")
    private String timezone;       // "Asia/Jakarta"
    private String zoneId;         // "WIB"
} 