package com.example.e_presensi.util.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_presensi.admin.dto.InformasiResponse;
import com.example.e_presensi.admin.service.InformasiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/running-text")
@Tag(name = "Running Text (Public)", description = "Endpoint publik untuk menampilkan informasi running text bertarget semua pengguna")
public class RunningTextPublicController {

    private static final Logger logger = LoggerFactory.getLogger(RunningTextPublicController.class);

    @Autowired
    private InformasiService informasiService;

    @GetMapping
    @Operation(summary = "Running text aktif (public)", description = "Mengambil daftar informasi aktif bertarget 'semua' untuk ditampilkan sebagai running text.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Berhasil mendapatkan running text aktif (target: semua)"),
        @ApiResponse(responseCode = "500", description = "Terjadi kesalahan server")
    })
    public ResponseEntity<?> getRunningTextPublic() {
        logger.info("=== MULAI: GET /api/running-text ===");
        try {
            List<InformasiResponse> informasiList = informasiService.getActiveInformasiByTipeUser("semua");
            logger.info("Berhasil mendapatkan {} data running text (public)", informasiList.size());
            logger.info("=== SELESAI: GET /api/running-text ===");
            return ResponseEntity.ok(informasiList);
        } catch (Exception e) {
            logger.error("Error saat mengambil running text (public)", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Terjadi kesalahan saat mendapatkan running text");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


