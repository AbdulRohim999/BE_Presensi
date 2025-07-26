package com.example.e_presensi.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class untuk menangani waktu dengan zona waktu Indonesia (WIB)
 */
public class DateTimeUtil {
    
    private static final ZoneId ZONE_JAKARTA = ZoneId.of("Asia/Jakarta");
    
    /**
     * Mendapatkan tanggal hari ini dalam zona waktu WIB
     */
    public static LocalDate getCurrentDateWIB() {
        return ZonedDateTime.now(ZONE_JAKARTA).toLocalDate();
    }
    
    /**
     * Mendapatkan waktu sekarang dalam zona waktu WIB
     */
    public static LocalDateTime getCurrentDateTimeWIB() {
        return ZonedDateTime.now(ZONE_JAKARTA).toLocalDateTime();
    }
    
    /**
     * Mendapatkan waktu (jam) sekarang dalam zona waktu WIB
     */
    public static LocalTime getCurrentTimeWIB() {
        return ZonedDateTime.now(ZONE_JAKARTA).toLocalTime();
    }
    
    /**
     * Mendapatkan ZonedDateTime sekarang dalam zona waktu WIB
     */
    public static ZonedDateTime getCurrentZonedDateTimeWIB() {
        return ZonedDateTime.now(ZONE_JAKARTA);
    }
    
    /**
     * Mengkonversi LocalDateTime ke ZonedDateTime dengan zona waktu WIB
     */
    public static ZonedDateTime toZonedDateTimeWIB(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZONE_JAKARTA);
    }
    
    /**
     * Mendapatkan ZoneId Jakarta
     */
    public static ZoneId getZoneJakarta() {
        return ZONE_JAKARTA;
    }
} 