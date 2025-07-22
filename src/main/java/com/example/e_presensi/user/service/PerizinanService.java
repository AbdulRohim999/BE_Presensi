package com.example.e_presensi.user.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public List<PerizinanResponse> getAllPerizinan() {
        List<Perizinan> perizinanList = perizinanRepository.findAll();
        return perizinanList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PerizinanResponse createPerizinan(Integer idUser, PerizinanRequest request) {
        logger.info("Membuat perizinan untuk user ID: {}", idUser);
        
        try {
            // Validasi tanggal
            LocalDate tanggalMulai = LocalDate.parse(request.getTanggalMulai(), DateTimeFormatter.ISO_DATE);
            LocalDate tanggalSelesai = LocalDate.parse(request.getTanggalSelesai(), DateTimeFormatter.ISO_DATE);
            
            logger.info("Tanggal mulai: {}, Tanggal selesai: {}", tanggalMulai, tanggalSelesai);
            
            if (tanggalMulai.isAfter(tanggalSelesai)) {
                logger.warn("Tanggal mulai setelah tanggal selesai");
                throw new IllegalArgumentException("Tanggal mulai harus sebelum tanggal selesai");
            }
            
            // Cari user profile untuk validasi
            UserProfile userProfile = userProfileRepository.findById(idUser)
                    .orElseThrow(() -> {
                        logger.error("User tidak ditemukan dengan ID: {}", idUser);
                        return new IllegalArgumentException("User tidak ditemukan");
                    });
            
            logger.info("User ditemukan: {} {} dengan role: {}", 
                       userProfile.getFirstname(), userProfile.getLastname(), userProfile.getRole());
            
            // Cek apakah sudah ada perizinan yang overlap
            boolean hasOverlap = perizinanRepository.existsByIdUserAndStatusAndTanggalMulaiBetweenOrTanggalSelesaiBetween(
                idUser,
                "Menunggu",
                tanggalMulai,
                tanggalSelesai,
                tanggalMulai,
                tanggalSelesai
            );
            
            if (hasOverlap) {
                logger.warn("Sudah ada perizinan yang overlap untuk user ID: {}", idUser);
                throw new IllegalArgumentException("Sudah ada perizinan yang overlap dengan tanggal yang dipilih");
            }
            
            // Buat objek perizinan
            Perizinan perizinan = Perizinan.builder()
                .idUser(idUser)
                .jenisIzin(request.getJenisIzin())
                .keterangan(request.getKeterangan())
                .tanggalMulai(tanggalMulai)
                .tanggalSelesai(tanggalSelesai)
                .status("Menunggu")
                .build();
            
            // Simpan ke database
            Perizinan savedPerizinan = perizinanRepository.save(perizinan);
            logger.info("Perizinan berhasil dibuat untuk user ID: {} dengan ID perizinan: {}", 
                       idUser, savedPerizinan.getIdPerizinan());
            
            return mapToResponse(savedPerizinan);
            
        } catch (Exception e) {
            logger.error("Error saat membuat perizinan untuk user ID: {}", idUser, e);
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
            if (request.getJenisIzin() == null || request.getJenisIzin().trim().isEmpty()) {
                throw new IllegalArgumentException("Jenis izin tidak boleh kosong");
            }
            
            String jenisIzin = request.getJenisIzin().toLowerCase().trim();
            if (!jenisIzin.equals("sakit") && !jenisIzin.equals("izin") && !jenisIzin.equals("cuti")) {
                throw new IllegalArgumentException("Jenis izin hanya boleh: sakit, izin, atau cuti");
            }
            
            // Validasi tanggal
            LocalDate tanggalMulai = LocalDate.parse(request.getTanggalMulai(), DateTimeFormatter.ISO_DATE);
            LocalDate tanggalSelesai = LocalDate.parse(request.getTanggalSelesai(), DateTimeFormatter.ISO_DATE);
            
            if (tanggalMulai.isAfter(tanggalSelesai)) {
                throw new IllegalArgumentException("Tanggal mulai tidak boleh setelah tanggal selesai");
            }
            
            // Cari user profile untuk validasi
            UserProfile userProfile = userProfileRepository.findById(idUser)
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
            
            // Cek apakah user sudah memiliki izin aktif pada rentang tanggal yang sama
            boolean izinExists = perizinanRepository.existsByIdUserAndStatusAndTanggalMulaiBetweenOrTanggalSelesaiBetween(
                    idUser, "Diterima",
                    tanggalMulai, tanggalSelesai,
                    tanggalMulai, tanggalSelesai);
            
            if (izinExists) {
                throw new IllegalStateException("Anda sudah memiliki izin yang aktif pada rentang tanggal tersebut");
            }
            
            // Buat objek perizinan baru
            Perizinan perizinan = Perizinan.builder()
                    .idUser(idUser)
                    .jenisIzin(jenisIzin)
                    .tanggalMulai(tanggalMulai)
                    .tanggalSelesai(tanggalSelesai)
                    .keterangan(request.getKeterangan())
                    .status("Menunggu")
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
        
        List<Perizinan> perizinanList = perizinanRepository.findByIdUser(idUser);
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
    
    @Transactional
    public PerizinanResponse createPerizinanWithLampiran(Integer idUser, PerizinanRequest request, MultipartFile lampiran) {
        LocalDate tanggalMulai = LocalDate.parse(request.getTanggalMulai());
        LocalDate tanggalSelesai = LocalDate.parse(request.getTanggalSelesai());
        String lampiranPath = null;
        if (lampiran != null && !lampiran.isEmpty()) {
            lampiranPath = fileStorageService.uploadFile(lampiran, "lampiran-perizinan/" + idUser);
        }
        Perizinan perizinan = Perizinan.builder()
            .idUser(idUser)
            .jenisIzin(request.getJenisIzin())
            .keterangan(request.getKeterangan())
            .tanggalMulai(tanggalMulai)
            .tanggalSelesai(tanggalSelesai)
            .status("Menunggu")
            .lampiran(lampiranPath)
            .build();
        Perizinan saved = perizinanRepository.save(perizinan);
        return mapToResponse(saved);
    }
    
    private PerizinanResponse mapToResponse(Perizinan perizinan) {
        // Cari user profile berdasarkan idUser
        UserProfile userProfile = userProfileRepository.findById(perizinan.getIdUser())
                .orElse(null);
        
        String namaUser = "Unknown User";
        if (userProfile != null) {
            namaUser = userProfile.getFirstname() + " " + userProfile.getLastname();
        }
        
        return PerizinanResponse.builder()
                .idPerizinan(perizinan.getIdPerizinan())
                .idUser(perizinan.getIdUser())
                .jenisIzin(perizinan.getJenisIzin())
                .tanggalMulai(perizinan.getTanggalMulai())
                .tanggalSelesai(perizinan.getTanggalSelesai())
                .keterangan(perizinan.getKeterangan())
                .status(perizinan.getStatus())
                .createAt(perizinan.getCreateAt())
                .namaUser(namaUser)
                .lampiran(perizinan.getLampiran())
                .build();
    }
}