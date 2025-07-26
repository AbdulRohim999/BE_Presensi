package com.example.e_presensi.util.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerTimeResponse {
    
    private String date;           // Format: "dd MMMM yyyy" (contoh: "15 Desember 2024")
    private String time;           // Format: "HH:mm:ss" (contoh: "14:30:25")
    private String dateTime;       // Format: "dd MMMM yyyy HH:mm:ss" (contoh: "15 Desember 2024 14:30:25")
    private String timestamp;      // Format ISO (contoh: "2024-12-15T14:30:25")
    private String timezone;       // "Asia/Jakarta"
    private String zoneId;         // "WIB"
    private String dayOfWeek;      // Format: "Senin", "Selasa", dll
    private String month;          // Format: "Desember"
    private String year;           // Format: "2024"
    private String hour;           // Format: "14"
    private String minute;         // Format: "30"
    private String second;         // Format: "25"
} 