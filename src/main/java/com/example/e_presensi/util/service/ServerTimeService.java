package com.example.e_presensi.util.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.e_presensi.util.DateTimeUtil;
import com.example.e_presensi.util.dto.ServerTimeResponse;
import com.example.e_presensi.util.dto.SimpleTimeResponse;

@Service
public class ServerTimeService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("mm");
    private static final DateTimeFormatter SECOND_FORMATTER = DateTimeFormatter.ofPattern("ss");
    
    /**
     * Mendapatkan waktu server lengkap dalam zona waktu WIB
     */
    public ServerTimeResponse getFullServerTimeWIB() {
        LocalDateTime now = DateTimeUtil.getCurrentDateTimeWIB();
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        LocalTime currentTime = DateTimeUtil.getCurrentTimeWIB();
        
        return ServerTimeResponse.builder()
                .date(today.format(DATE_FORMATTER))
                .time(currentTime.format(TIME_FORMATTER))
                .dateTime(now.format(DATETIME_FORMATTER))
                .timestamp(now.toString())
                .timezone("Asia/Jakarta")
                .zoneId("WIB")
                .dayOfWeek(today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID")))
                .month(today.format(MONTH_FORMATTER))
                .year(today.format(YEAR_FORMATTER))
                .hour(currentTime.format(HOUR_FORMATTER))
                .minute(currentTime.format(MINUTE_FORMATTER))
                .second(currentTime.format(SECOND_FORMATTER))
                .build();
    }
    
    /**
     * Mendapatkan tanggal server dalam zona waktu WIB
     */
    public ServerTimeResponse getServerDateWIB() {
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        
        return ServerTimeResponse.builder()
                .date(today.format(DATE_FORMATTER))
                .timestamp(today.toString())
                .timezone("Asia/Jakarta")
                .zoneId("WIB")
                .dayOfWeek(today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID")))
                .month(today.format(MONTH_FORMATTER))
                .year(today.format(YEAR_FORMATTER))
                .build();
    }
    
    /**
     * Mendapatkan waktu server dalam zona waktu WIB
     */
    public ServerTimeResponse getServerTimeOnlyWIB() {
        LocalTime currentTime = DateTimeUtil.getCurrentTimeWIB();
        
        return ServerTimeResponse.builder()
                .time(currentTime.format(TIME_FORMATTER))
                .timestamp(currentTime.toString())
                .timezone("Asia/Jakarta")
                .zoneId("WIB")
                .hour(currentTime.format(HOUR_FORMATTER))
                .minute(currentTime.format(MINUTE_FORMATTER))
                .second(currentTime.format(SECOND_FORMATTER))
                .build();
    }
    
    /**
     * Mendapatkan informasi zona waktu server
     */
    public ServerTimeResponse getServerTimezoneInfo() {
        LocalDateTime now = DateTimeUtil.getCurrentDateTimeWIB();
        
        return ServerTimeResponse.builder()
                .timezone("Asia/Jakarta")
                .zoneId("WIB")
                .timestamp(now.toString())
                .build();
    }
    
    /**
     * Mendapatkan waktu dan tanggal server Indonesia WIB (sederhana)
     */
    public SimpleTimeResponse getSimpleServerTimeWIB() {
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        LocalTime currentTime = DateTimeUtil.getCurrentTimeWIB();
        String day = today.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("id", "ID"));
        
        return SimpleTimeResponse.builder()
                .day(day)
                .date(today.format(DATE_FORMATTER))
                .time(currentTime.format(TIME_FORMATTER))
                .timezone("Asia/Jakarta")
                .zoneId("WIB")
                .build();
    }
} 