package com.example.e_presensi.util.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.util.dto.ServerTimeResponse;
import com.example.e_presensi.util.dto.SimpleTimeResponse;
import com.example.e_presensi.util.service.ServerTimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/server-time")
@Tag(name = "Server Time", description = "API untuk mendapatkan waktu server Indonesia WIB")
public class ServerTimeController {
    
    @Autowired
    private ServerTimeService serverTimeService;
    
    @GetMapping("/wib")
    @Operation(summary = "Mendapatkan waktu server Indonesia WIB lengkap", 
               description = "Mengembalikan waktu server dalam zona waktu Indonesia (WIB) dalam berbagai format")
    public ServerTimeResponse getServerTimeWIB() {
        return serverTimeService.getFullServerTimeWIB();
    }
    
    @GetMapping("/wib/date")
    @Operation(summary = "Mendapatkan tanggal server Indonesia WIB", 
               description = "Mengembalikan tanggal server dalam zona waktu Indonesia (WIB)")
    public ServerTimeResponse getServerDateWIB() {
        return serverTimeService.getServerDateWIB();
    }
    
    @GetMapping("/wib/time")
    @Operation(summary = "Mendapatkan waktu server Indonesia WIB", 
               description = "Mengembalikan waktu server dalam zona waktu Indonesia (WIB)")
    public ServerTimeResponse getServerTimeOnlyWIB() {
        return serverTimeService.getServerTimeOnlyWIB();
    }
    
    @GetMapping("/wib/timezone")
    @Operation(summary = "Mendapatkan informasi zona waktu server", 
               description = "Mengembalikan informasi zona waktu server Indonesia (WIB)")
    public ServerTimeResponse getServerTimezoneInfo() {
        return serverTimeService.getServerTimezoneInfo();
    }
    
    @GetMapping("/wib/day")
    @Operation(summary = "Mendapatkan hari dalam seminggu server Indonesia WIB", 
               description = "Mengembalikan hari dalam seminggu dalam bahasa Indonesia")
    public ServerTimeResponse getServerDayWIB() {
        return serverTimeService.getServerDateWIB();
    }
    
    @GetMapping("/wib/now")
    @Operation(summary = "Mendapatkan waktu sekarang server Indonesia WIB", 
               description = "Alias untuk endpoint /wib - mengembalikan waktu server lengkap")
    public ServerTimeResponse getServerNowWIB() {
        return serverTimeService.getFullServerTimeWIB();
    }
    
    @GetMapping("/wib/simple")
    @Operation(summary = "Mendapatkan waktu dan tanggal server Indonesia WIB (sederhana)", 
               description = "Mengembalikan hanya waktu dan tanggal server dalam format yang sederhana")
    public SimpleTimeResponse getSimpleServerTimeWIB() {
        return serverTimeService.getSimpleServerTimeWIB();
    }
} 