package com.example.e_presensi.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.user.dto.PerizinanRequest;
import com.example.e_presensi.user.dto.PerizinanResponse;
import com.example.e_presensi.user.model.Perizinan;
import com.example.e_presensi.user.repository.PerizinanRepository;

@Service
public class PerizinanService {
    
    private static final Logger logger = LoggerFactory.getLogger(PerizinanService.class);
    
    @Autowired
    private PerizinanRepository perizinanRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    // Hapus referensi ke FileStorageService
    // @Autowired
    // private FileStorageService fileStorageService;
    
    public List<PerizinanResponse> getAllPerizinan() {
        List<Perizinan> perizinanList = perizinanRepository.findAll();
        return perizinanList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PerizinanResponse createPerizinan(Integer idUser, PerizinanRequest request) {
        try {
            // Validasi tanggal
            LocalDate tanggalMulai = LocalDate.parse(request.getTanggalMulai(), DateTimeFormatter.ISO_DATE);
            LocalDate tanggalSelesai = LocalDate.parse(request.getTanggalSelesai(), DateTimeFormatter.ISO_DATE);
            
            if (tanggalMulai.isAfter(tanggalSelesai)) {
                throw new IllegalArgumentException("Tanggal mulai harus sebelum tanggal selesai");
            }
            
            // Cari user profile
            UserProfile userProfile = userProfileRepository.findById(idUser)
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
            
            // Cek apakah sudah ada perizinan yang overlap
            boolean hasOverlap = perizinanRepository.existsByUserProfileAndStatusAndTanggalMulaiBetweenOrTanggalSelesaiBetween(
                userProfile,
                "pending",
                tanggalMulai,
                tanggalSelesai,
                tanggalMulai,
                tanggalSelesai
            );
            
            if (hasOverlap) {
                throw new IllegalArgumentException("Sudah ada perizinan yang overlap dengan tanggal yang dipilih");
            }
            
            // Buat objek perizinan
            Perizinan perizinan = Perizinan.builder()
                .userProfile(userProfile)
                .jenisIzin(request.getJenisPerizinan())
                .keterangan(request.getAlasan())
                .tanggalMulai(tanggalMulai)
                .tanggalSelesai(tanggalSelesai)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            // Simpan ke database
            Perizinan savedPerizinan = perizinanRepository.save(perizinan);
            logger.info("Perizinan berhasil dibuat untuk user ID: {}", idUser);
            
            return mapToResponse(savedPerizinan);
            
        } catch (Exception e) {
            logger.error("Error saat membuat perizinan", e);
            throw new RuntimeException("Gagal membuat perizinan: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deletePerizinan(Integer idPerizinan) {
        try {
            Perizinan perizinan = perizinanRepository.findById(idPerizinan)
                    .orElseThrow(() -> new IllegalArgumentException("Perizinan tidak ditemukan"));
            
            perizinanRepository.delete(perizinan);
            logger.info("Perizinan berhasil dihapus: {}", idPerizinan);
            
        } catch (Exception e) {
            logger.error("Error saat menghapus perizinan", e);
            throw new RuntimeException("Gagal menghapus perizinan: " + e.getMessage());
        }
    }
    
    @Transactional
    public PerizinanResponse ajukanIzin(Integer idUser, PerizinanRequest request) {
        try {
            // Validasi jenis izin
            if (request.getJenisPerizinan() == null || request.getJenisPerizinan().trim().isEmpty()) {
                throw new IllegalArgumentException("Jenis izin tidak boleh kosong");
            }
            
            String jenisIzin = request.getJenisPerizinan().toLowerCase().trim();
            if (!jenisIzin.equals("sakit") && !jenisIzin.equals("izin") && !jenisIzin.equals("cuti")) {
                throw new IllegalArgumentException("Jenis izin hanya boleh: sakit, izin, atau cuti");
            }
            
            // Validasi tanggal
            LocalDate tanggalMulai = LocalDate.parse(request.getTanggalMulai(), DateTimeFormatter.ISO_DATE);
            LocalDate tanggalSelesai = LocalDate.parse(request.getTanggalSelesai(), DateTimeFormatter.ISO_DATE);
            
            if (tanggalMulai.isAfter(tanggalSelesai)) {
                throw new IllegalArgumentException("Tanggal mulai tidak boleh setelah tanggal selesai");
            }
            
            // Cari user profile
            UserProfile userProfile = userProfileRepository.findById(idUser)
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
            
            // Cek apakah user sudah memiliki izin aktif pada rentang tanggal yang sama
            boolean izinExists = perizinanRepository.existsByUserProfileAndStatusAndTanggalMulaiBetweenOrTanggalSelesaiBetween(
                    userProfile, "Diterima",
                    tanggalMulai, tanggalSelesai,
                    tanggalMulai, tanggalSelesai);
            
            if (izinExists) {
                throw new IllegalStateException("Anda sudah memiliki izin yang aktif pada rentang tanggal tersebut");
            }
            
            // Buat objek perizinan baru
            Perizinan perizinan = Perizinan.builder()
                    .userProfile(userProfile)
                    .jenisIzin(jenisIzin)
                    .tanggalMulai(tanggalMulai)
                    .tanggalSelesai(tanggalSelesai)
                    .keterangan(request.getAlasan())
                    .status("Menunggu")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Simpan ke database
            Perizinan savedPerizinan = perizinanRepository.save(perizinan);
            logger.info("Perizinan berhasil dibuat untuk user ID: {}", idUser);
            
            return mapToResponse(savedPerizinan);
            
        } catch (Exception e) {
            logger.error("Error saat membuat perizinan", e);
            throw new RuntimeException("Gagal membuat perizinan: " + e.getMessage());
        }
    }
    
    public List<PerizinanResponse> getRiwayatIzin(Integer idUser) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }
        
        List<Perizinan> perizinanList = perizinanRepository.findByUserProfile(userProfileOpt.get());
        return perizinanList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PerizinanResponse getDetailIzin(Integer idPerizinan) {
        Optional<Perizinan> perizinanOpt = perizinanRepository.findById(idPerizinan);
        if (perizinanOpt.isPresent()) {
            return mapToResponse(perizinanOpt.get());
        }
        return null;
    }
    
    public List<PerizinanResponse> getAllPerizinanByStatus(String status) {
        List<Perizinan> perizinanList = perizinanRepository.findByStatus(status);
        return perizinanList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PerizinanResponse updateStatusPerizinan(Integer idPerizinan, String status) {
        // Validasi status
        if (!status.equals("Menunggu") && !status.equals("Diterima") && !status.equals("Ditolak")) {
            throw new IllegalArgumentException("Status hanya boleh: Menunggu, Diterima, atau Ditolak");
        }
        
        Optional<Perizinan> perizinanOpt = perizinanRepository.findById(idPerizinan);
        if (perizinanOpt.isPresent()) {
            Perizinan perizinan = perizinanOpt.get();
            perizinan.setStatus(status);
            
            Perizinan updatedPerizinan = perizinanRepository.save(perizinan);
            return mapToResponse(updatedPerizinan);
        }
        return null;
    }
    
    private PerizinanResponse mapToResponse(Perizinan perizinan) {
        UserProfile userProfile = perizinan.getUserProfile();
        
        return PerizinanResponse.builder()
                .idPerizinan(perizinan.getIdPerizinan())
                .idUser(userProfile.getId_user())
                .jenisIzin(perizinan.getJenisIzin())
                .tanggalMulai(perizinan.getTanggalMulai())
                .tanggalSelesai(perizinan.getTanggalSelesai())
                .keterangan(perizinan.getKeterangan())
                .status(perizinan.getStatus())
                .createdAt(perizinan.getCreatedAt())
                .updatedAt(perizinan.getUpdatedAt())
                .namaUser(userProfile.getFirstname() + " " + userProfile.getLastname())
                .build();
    }
}