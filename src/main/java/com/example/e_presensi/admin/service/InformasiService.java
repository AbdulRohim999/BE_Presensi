package com.example.e_presensi.admin.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.admin.dto.InformasiRequest;
import com.example.e_presensi.admin.dto.InformasiResponse;
import com.example.e_presensi.admin.model.Informasi;
import com.example.e_presensi.admin.repository.InformasiRepository;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.util.DateTimeUtil;

@Service
public class InformasiService {
    
    private static final Logger logger = LoggerFactory.getLogger(InformasiService.class);
    
    @Autowired
    private InformasiRepository informasiRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Transactional
    public InformasiResponse createInformasi(InformasiRequest request, String createdBy) {
        logger.info("Membuat informasi baru oleh: {}", createdBy);
        
        try {
            // Validasi tanggal
            if (request.getTanggalMulai().isAfter(request.getTanggalSelesai())) {
                throw new IllegalArgumentException("Tanggal mulai tidak boleh setelah tanggal selesai");
            }
            
            // Validasi target tipe user
            String targetTipeUser = request.getTargetTipeUser().toLowerCase().trim();
            if (!targetTipeUser.equals("dosen") && !targetTipeUser.equals("karyawan") && !targetTipeUser.equals("semua")) {
                throw new IllegalArgumentException("Target tipe user hanya boleh: dosen, karyawan, atau semua");
            }
            
            // Buat objek informasi
            Informasi informasi = Informasi.builder()
                .judul(request.getJudul())
                .keterangan(request.getKeterangan())
                .kategori(request.getKategori())
                .tanggalMulai(request.getTanggalMulai())
                .tanggalSelesai(request.getTanggalSelesai())
                .createdBy(createdBy)
                .targetTipeUser(targetTipeUser)
                .build();
            
            // Simpan ke database
            Informasi savedInformasi = informasiRepository.save(informasi);
            logger.info("Informasi berhasil dibuat dengan ID: {}", savedInformasi.getInformasiId());
            
            return mapToResponse(savedInformasi);
            
        } catch (Exception e) {
            logger.error("Error saat membuat informasi", e);
            throw new RuntimeException("Gagal membuat informasi: " + e.getMessage());
        }
    }
    
    public List<InformasiResponse> getAllInformasi() {
        logger.info("Mengambil semua informasi");
        
        List<Informasi> informasiList = informasiRepository.findAllByOrderByCreatedAtDesc();
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InformasiResponse> getActiveInformasi() {
        logger.info("Mengambil informasi yang aktif");
        
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        List<Informasi> informasiList = informasiRepository.findActiveInformasi(today);
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InformasiResponse> getActiveInformasiForUser(String username) {
        logger.info("Mengambil informasi aktif untuk user: {}", username);
        
        UserProfile userProfile = userProfileRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan email: " + username));
        
        String tipeUser = userProfile.getTipeUser();
        if (tipeUser == null) {
            logger.warn("Tipe user null untuk user: {}", username);
            return new ArrayList<>();
        }

        // Normalisasi tipeUser: hapus spasi di awal/akhir dan ubah ke huruf kecil
        String normalizedTipeUser = tipeUser.trim().toLowerCase();

        if (!normalizedTipeUser.equals("dosen") && !normalizedTipeUser.equals("karyawan")) {
            logger.warn("Tipe user tidak valid. User: {}, Tipe dari DB: '{}', Tipe Normalisasi: '{}'", username, tipeUser, normalizedTipeUser);
            return new ArrayList<>();
        }
        
        logger.info("Tipe user dinormalisasi menjadi '{}' untuk user: {}", normalizedTipeUser, username);
        
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        
        // Gunakan tipe user yang sudah dinormalisasi untuk query
        List<Informasi> informasiList = informasiRepository.findActiveInformasiByTipeUser(today, normalizedTipeUser);
        
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InformasiResponse> getAllInformasiForUser(String username) {
        logger.info("Mengambil SEMUA informasi untuk user: {}", username);
    
        UserProfile userProfile = userProfileRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan email: " + username));
    
        String tipeUser = userProfile.getTipeUser();
        if (tipeUser == null) {
            logger.warn("Tipe user null untuk user: {}", username);
            return new ArrayList<>();
        }
    
        // Normalisasi tipeUser
        String normalizedTipeUser = tipeUser.trim().toLowerCase();
    
        if (!normalizedTipeUser.equals("dosen") && !normalizedTipeUser.equals("karyawan")) {
            logger.warn("Tipe user tidak valid saat mengambil semua informasi. User: {}", username);
            return new ArrayList<>();
        }
    
        List<Informasi> informasiList = informasiRepository.findAllByTipeUser(normalizedTipeUser);
    
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<InformasiResponse> getActiveInformasiByTipeUser(String tipeUser) {
        logger.info("Mengambil informasi yang aktif untuk tipe user: {}", tipeUser);
        
        LocalDate today = DateTimeUtil.getCurrentDateWIB();
        List<Informasi> informasiList = informasiRepository.findActiveInformasiByTipeUser(today, tipeUser.toLowerCase());
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public InformasiResponse getInformasiById(Integer informasiId) {
        logger.info("Mengambil informasi dengan ID: {}", informasiId);
        
        Optional<Informasi> informasiOpt = informasiRepository.findById(informasiId);
        if (informasiOpt.isPresent()) {
            return mapToResponse(informasiOpt.get());
        }
        return null;
    }
    
    public List<InformasiResponse> searchInformasiByJudul(String judul) {
        logger.info("Mencari informasi dengan judul: {}", judul);
        
        List<Informasi> informasiList = informasiRepository.findByJudulContainingIgnoreCaseOrderByCreatedAtDesc(judul);
        return informasiList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public InformasiResponse updateInformasi(Integer informasiId, InformasiRequest request) {
        logger.info("Mengupdate informasi dengan ID: {}", informasiId);
        
        try {
            Optional<Informasi> informasiOpt = informasiRepository.findById(informasiId);
            if (!informasiOpt.isPresent()) {
                throw new IllegalArgumentException("Informasi tidak ditemukan");
            }
            
            Informasi informasi = informasiOpt.get();
            
            // Validasi tanggal
            if (request.getTanggalMulai().isAfter(request.getTanggalSelesai())) {
                throw new IllegalArgumentException("Tanggal mulai tidak boleh setelah tanggal selesai");
            }
            
            // Validasi target tipe user
            String targetTipeUser = request.getTargetTipeUser().toLowerCase().trim();
            if (!targetTipeUser.equals("dosen") && !targetTipeUser.equals("karyawan") && !targetTipeUser.equals("semua")) {
                throw new IllegalArgumentException("Target tipe user hanya boleh: dosen, karyawan, atau semua");
            }
            
            // Update field
            informasi.setJudul(request.getJudul());
            informasi.setKeterangan(request.getKeterangan());
            informasi.setKategori(request.getKategori());
            informasi.setTanggalMulai(request.getTanggalMulai());
            informasi.setTanggalSelesai(request.getTanggalSelesai());
            informasi.setTargetTipeUser(targetTipeUser);
            
            // Simpan perubahan
            Informasi updatedInformasi = informasiRepository.save(informasi);
            logger.info("Informasi berhasil diupdate dengan ID: {}", updatedInformasi.getInformasiId());
            
            return mapToResponse(updatedInformasi);
            
        } catch (Exception e) {
            logger.error("Error saat mengupdate informasi", e);
            throw new RuntimeException("Gagal mengupdate informasi: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deleteInformasi(Integer informasiId) {
        logger.info("Menghapus informasi dengan ID: {}", informasiId);
        
        try {
            if (!informasiRepository.existsById(informasiId)) {
                throw new IllegalArgumentException("Informasi tidak ditemukan");
            }
            
            informasiRepository.deleteById(informasiId);
            logger.info("Informasi berhasil dihapus dengan ID: {}", informasiId);
            
        } catch (Exception e) {
            logger.error("Error saat menghapus informasi", e);
            throw new RuntimeException("Gagal menghapus informasi: " + e.getMessage());
        }
    }
    
    private InformasiResponse mapToResponse(Informasi informasi) {
        return InformasiResponse.builder()
                .informasiId(informasi.getInformasiId())
                .judul(informasi.getJudul())
                .keterangan(informasi.getKeterangan())
                .kategori(informasi.getKategori())
                .tanggalMulai(informasi.getTanggalMulai())
                .tanggalSelesai(informasi.getTanggalSelesai())
                .createdBy(informasi.getCreatedBy())
                .targetTipeUser(informasi.getTargetTipeUser())
                .createdAt(informasi.getCreatedAt())
                .build();
    }
} 