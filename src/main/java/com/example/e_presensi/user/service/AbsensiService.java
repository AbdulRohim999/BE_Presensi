package com.example.e_presensi.user.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.user.dto.AbsensiRequest;
import com.example.e_presensi.user.dto.AbsensiResponse;
import com.example.e_presensi.user.dto.LaporanKehadiranUserResponse;
import com.example.e_presensi.user.model.Absensi;
import com.example.e_presensi.user.repository.AbsensiRepository;
import com.example.e_presensi.util.DateTimeUtil;

@Service
public class AbsensiService {

    private static final Logger logger = LoggerFactory.getLogger(AbsensiService.class);
    
    // Definisi zona waktu Indonesia (WIB)
    private static final ZoneId ZONE_JAKARTA = ZoneId.of("Asia/Jakarta");

    // Definisi waktu absensi
    private static final LocalTime PAGI_MULAI = LocalTime.of(7, 30);
    private static final LocalTime PAGI_SELESAI = LocalTime.of(8, 15);
    
    private static final LocalTime SIANG_MULAI = LocalTime.of(12, 0);
    private static final LocalTime SIANG_SELESAI = LocalTime.of(13, 30);
    
    private static final LocalTime SORE_MULAI = LocalTime.of(16, 0);
    private static final LocalTime SORE_SELESAI = LocalTime.of(21, 0);

    @Autowired
    private AbsensiRepository absensiRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public AbsensiResponse melakukanAbsensi(Integer idUser, AbsensiRequest request) {
        logger.info("Melakukan absensi untuk user ID: {} dengan tipe: {}", idUser, request.getTipeAbsen());
        
        // Validasi tipe absen
        String tipeAbsen = request.getTipeAbsen().toLowerCase();
        if (!tipeAbsen.equals("pagi") && !tipeAbsen.equals("siang") && !tipeAbsen.equals("sore")) {
            logger.warn("Tipe absen tidak valid: {}", tipeAbsen);
            return null;
        }
        
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Menggunakan zona waktu WIB untuk tanggal dan waktu
        ZonedDateTime waktuSekarangWIB = DateTimeUtil.getCurrentZonedDateTimeWIB();
        LocalDate hariIni = waktuSekarangWIB.toLocalDate();
        LocalDateTime waktuSekarang = waktuSekarangWIB.toLocalDateTime();
        
        // Cari absensi hari ini
        Optional<Absensi> absensiOpt = absensiRepository.findByUserProfileAndTanggal(userProfile, hariIni);
        Absensi absensi;
        
        if (absensiOpt.isPresent()) {
            absensi = absensiOpt.get();
        } else {
            // Buat absensi baru jika belum ada
            absensi = new Absensi();
            absensi.setUserProfile(userProfile);
            absensi.setTanggal(hariIni);
            absensi.setCreateAt(waktuSekarang);
            absensi.setStatus("Belum Lengkap");
        }
        
        // Update waktu absensi sesuai tipe
        String statusAbsen = "Terlambat"; // Default status
        LocalTime waktuSekarangTime = waktuSekarang.toLocalTime();
        
        switch (tipeAbsen) {
            case "pagi":
                // Cek apakah sudah absen pagi
                if (absensi.getAbsenPagi() != null) {
                    logger.warn("User sudah melakukan absen pagi hari ini");
                    return convertToResponse(absensi);
                }
                
                // Cek apakah tepat waktu
                if (waktuSekarangTime.isAfter(PAGI_MULAI.minusSeconds(1)) && 
                    waktuSekarangTime.isBefore(PAGI_SELESAI.plusSeconds(1))) {
                    statusAbsen = "Tepat Waktu";
                }
                
                absensi.setAbsenPagi(waktuSekarang);
                break;
                
            case "siang":
                // Cek apakah sudah absen siang
                if (absensi.getAbsenSiang() != null) {
                    logger.warn("User sudah melakukan absen siang hari ini");
                    return convertToResponse(absensi);
                }
                
                // Cek apakah tepat waktu
                if (waktuSekarangTime.isAfter(SIANG_MULAI.minusSeconds(1)) && 
                    waktuSekarangTime.isBefore(SIANG_SELESAI.plusSeconds(1))) {
                    statusAbsen = "Tepat Waktu";
                }
                
                absensi.setAbsenSiang(waktuSekarang);
                break;
                
            case "sore":
                // Cek apakah sudah absen sore
                if (absensi.getAbsenSore() != null) {
                    logger.warn("User sudah melakukan absen sore hari ini");
                    return convertToResponse(absensi);
                }
                
                // Cek apakah tepat waktu
                if (waktuSekarangTime.isAfter(SORE_MULAI.minusSeconds(1)) && 
                    waktuSekarangTime.isBefore(SORE_SELESAI.plusSeconds(1))) {
                    statusAbsen = "Tepat Waktu";
                }
                
                absensi.setAbsenSore(waktuSekarang);
                break;
        }
        
        // Update status absensi keseluruhan
        updateStatusAbsensi(absensi);
        
        // Update waktu update
        absensi.setUpdateAt(waktuSekarang);
        
        // Simpan absensi
        absensi = absensiRepository.save(absensi);
        logger.info("Absensi berhasil disimpan dengan ID: {}", absensi.getIdAbsensi());
        
        return convertToResponse(absensi);
    }
    
    private void updateStatusAbsensi(Absensi absensi) {
        LocalDate tanggalAbsensi = absensi.getTanggal();
        LocalDate hariIni = DateTimeUtil.getCurrentDateWIB();
        LocalTime waktuSekarang = DateTimeUtil.getCurrentTimeWIB();
        DayOfWeek hariAbsensi = tanggalAbsensi.getDayOfWeek();
        
        // Cek apakah hari ini dan masih dalam waktu absensi
        boolean isHariIni = tanggalAbsensi.equals(hariIni);
        boolean isMasihWaktuAbsensi = waktuSekarang.isBefore(LocalTime.of(21, 0));
        
        // Tentukan jumlah absensi yang diperlukan berdasarkan hari
        int jumlahAbsensiDiperlukan;
        boolean isSabtu = hariAbsensi == DayOfWeek.SATURDAY;
        
        if (isSabtu) {
            jumlahAbsensiDiperlukan = 2; // Sabtu: pagi dan siang
        } else if (hariAbsensi == DayOfWeek.SUNDAY) {
            jumlahAbsensiDiperlukan = 0; // Minggu: tidak ada absensi
        } else {
            jumlahAbsensiDiperlukan = 3; // Senin-Jumat: pagi, siang, sore
        }
        
        // Jika hari Minggu, langsung set Invalid
        if (hariAbsensi == DayOfWeek.SUNDAY) {
            absensi.setStatus("Invalid");
            return;
        }
        
        // Hitung jumlah absensi yang sudah dilakukan
        int jumlahAbsensiDilakukan = 0;
        if (absensi.getAbsenPagi() != null) jumlahAbsensiDilakukan++;
        if (absensi.getAbsenSiang() != null) jumlahAbsensiDilakukan++;
        if (absensi.getAbsenSore() != null) jumlahAbsensiDilakukan++;
        
        // Cek apakah semua absensi yang diperlukan sudah dilakukan
        boolean semuaAbsensiDilakukan = jumlahAbsensiDilakukan >= jumlahAbsensiDiperlukan;
        
        // Jika hari ini dan masih dalam waktu absensi (sebelum jam 21:00) dan belum lengkap
        if (isHariIni && isMasihWaktuAbsensi && !semuaAbsensiDilakukan) {
            absensi.setStatus("Pending");
            return;
        }
        
        // Jika belum semua absensi dilakukan (setelah jam 21:00 atau bukan hari ini)
        if (!semuaAbsensiDilakukan) {
            absensi.setStatus("Invalid");
            return;
        }
        
        // Cek ketepatan waktu untuk setiap absensi yang diperlukan
        boolean semuaTepatWaktu = true;
        
        // Cek absen pagi
        if (absensi.getAbsenPagi() != null) {
            LocalTime waktuAbsenPagi = absensi.getAbsenPagi().toLocalTime();
            boolean pagiTepatWaktu = (waktuAbsenPagi.isAfter(PAGI_MULAI.minusSeconds(1)) && 
                                     waktuAbsenPagi.isBefore(PAGI_SELESAI.plusSeconds(1)));
            if (!pagiTepatWaktu) {
                semuaTepatWaktu = false;
            }
        } else if (jumlahAbsensiDiperlukan > 0) {
            // Jika absen pagi diperlukan tapi tidak ada
            semuaTepatWaktu = false;
        }
        
        // Cek absen siang
        if (absensi.getAbsenSiang() != null) {
            LocalTime waktuAbsenSiang = absensi.getAbsenSiang().toLocalTime();
            boolean siangTepatWaktu = (waktuAbsenSiang.isAfter(SIANG_MULAI.minusSeconds(1)) && 
                                      waktuAbsenSiang.isBefore(SIANG_SELESAI.plusSeconds(1)));
            if (!siangTepatWaktu) {
                semuaTepatWaktu = false;
            }
        } else if (jumlahAbsensiDiperlukan > 1) {
            // Jika absen siang diperlukan tapi tidak ada
            semuaTepatWaktu = false;
        }
        
        // Cek absen sore (hanya untuk Senin-Jumat)
        if (absensi.getAbsenSore() != null) {
            LocalTime waktuAbsenSore = absensi.getAbsenSore().toLocalTime();
            boolean soreTepatWaktu = (waktuAbsenSore.isAfter(SORE_MULAI.minusSeconds(1)) && 
                                     waktuAbsenSore.isBefore(SORE_SELESAI.plusSeconds(1)));
            if (!soreTepatWaktu) {
                semuaTepatWaktu = false;
            }
        } else if (jumlahAbsensiDiperlukan > 2) {
            // Jika absen sore diperlukan tapi tidak ada
            semuaTepatWaktu = false;
        }
        
        // Set status berdasarkan ketepatan waktu
        if (semuaTepatWaktu) {
            absensi.setStatus("Valid");
        } else {
            absensi.setStatus("Invalid");
        }
    }
    
    public AbsensiResponse getAbsensiHariIni(Integer idUser) {
        logger.info("Mengambil absensi hari ini untuk user ID: {}", idUser);
        
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        // Menggunakan zona waktu WIB
        LocalDate hariIni = DateTimeUtil.getCurrentDateWIB();
        
        Optional<Absensi> absensiOpt = absensiRepository.findByUserProfileAndTanggal(userProfile, hariIni);
        if (!absensiOpt.isPresent()) {
            logger.info("Belum ada absensi hari ini untuk user ID: {}", idUser);
            return null;
        }
        
        return convertToResponse(absensiOpt.get());
    }
    
    public List<AbsensiResponse> getRiwayatAbsensi(Integer idUser) {
        logger.info("Mengambil riwayat absensi untuk user ID: {}", idUser);
        
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return List.of();
        }
        
        UserProfile userProfile = userProfileOpt.get();
        List<Absensi> absensiList = absensiRepository.findByUserProfileOrderByTanggalDesc(userProfile);
        
        return absensiList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<AbsensiResponse> getRiwayatAbsensiByDateRange(Integer idUser, LocalDate startDate, LocalDate endDate) {
        logger.info("Mengambil riwayat absensi untuk user ID: {} dari tanggal {} sampai {}", idUser, startDate, endDate);
        
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return List.of();
        }
        
        UserProfile userProfile = userProfileOpt.get();
        List<Absensi> absensiList = absensiRepository.findByUserProfileAndDateRange(userProfile, startDate, endDate);
        
        return absensiList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private AbsensiResponse convertToResponse(Absensi absensi) {
        String statusPagi = "Belum Absen";
        String statusSiang = "Belum Absen";
        String statusSore = "Belum Absen";
        
        if (absensi.getAbsenPagi() != null) {
            LocalTime waktuAbsenPagi = absensi.getAbsenPagi().toLocalTime();
            statusPagi = (waktuAbsenPagi.isAfter(PAGI_MULAI.minusSeconds(1)) && 
                         waktuAbsenPagi.isBefore(PAGI_SELESAI.plusSeconds(1))) 
                ? "Tepat Waktu" : "Terlambat";
        }
        
        if (absensi.getAbsenSiang() != null) {
            LocalTime waktuAbsenSiang = absensi.getAbsenSiang().toLocalTime();
            statusSiang = (waktuAbsenSiang.isAfter(SIANG_MULAI.minusSeconds(1)) && 
                          waktuAbsenSiang.isBefore(SIANG_SELESAI.plusSeconds(1))) 
                ? "Tepat Waktu" : "Terlambat";
        }
        
        if (absensi.getAbsenSore() != null) {
            LocalTime waktuAbsenSore = absensi.getAbsenSore().toLocalTime();
            statusSore = (waktuAbsenSore.isAfter(SORE_MULAI.minusSeconds(1)) && 
                         waktuAbsenSore.isBefore(SORE_SELESAI.plusSeconds(1))) 
                ? "Tepat Waktu" : "Terlambat";
        }
        
        return new AbsensiResponse(
            absensi.getIdAbsensi(),
            absensi.getTanggal(),
            absensi.getAbsenPagi(),
            statusPagi,
            absensi.getAbsenSiang(),
            statusSiang,
            absensi.getAbsenSore(),
            statusSore,
            absensi.getStatus()
        );
    }
    
    public List<AbsensiResponse> getAllAbsensiHariIni() {
        logger.info("Mengambil semua absensi hari ini");
        
        // Menggunakan zona waktu WIB
        LocalDate hariIni = DateTimeUtil.getCurrentDateWIB();
        List<Absensi> absensiList = absensiRepository.findByTanggal(hariIni);
        
        return absensiList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Mendapatkan laporan kehadiran user berdasarkan minggu
     * @param idUser ID user yang login
     * @param weekNumber Nomor minggu (1-5)
     * @param month Bulan (1-12)
     * @param year Tahun
     * @return LaporanKehadiranUserResponse
     */
    public LaporanKehadiranUserResponse getLaporanKehadiranUserByWeek(Integer idUser, int weekNumber, int month, int year) {
        logger.info("Mengambil laporan kehadiran user ID {} untuk minggu ke-{} bulan {} tahun {}", idUser, weekNumber, month, year);
        
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Menentukan tanggal awal dan akhir minggu
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        
        // Mencari tanggal awal minggu yang diminta
        LocalDate startDate = firstDayOfMonth;
        for (int i = 1; i < weekNumber; i++) {
            startDate = startDate.plusWeeks(1);
        }
        
        // Jika startDate sudah di bulan berikutnya, berarti minggu yang diminta tidak ada di bulan tersebut
        if (startDate.getMonthValue() != month) {
            return null;
        }
        
        // Menentukan tanggal akhir minggu (6 hari setelah tanggal awal atau akhir bulan, mana yang lebih dulu)
        LocalDate endDate = startDate.plusDays(6);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        if (endDate.isAfter(lastDayOfMonth)) {
            endDate = lastDayOfMonth;
        }
        
        // Mendapatkan absensi user dalam rentang tanggal
        List<Absensi> userAbsensi = absensiRepository.findByUserProfileAndDateRange(userProfile, startDate, endDate);
        
        // Menghitung jumlah berdasarkan status
        int tepatWaktu = 0;
        int terlambat = 0;
        int tidakMasuk = 0;
        int izin = 0; // Jika ada fitur izin
        
        // Menghitung jumlah hari kerja dalam rentang tanggal (Senin-Jumat)
        List<LocalDate> workDays = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // Memeriksa status absensi untuk setiap hari kerja
        for (LocalDate workDay : workDays) {
            boolean found = false;
            for (Absensi absensi : userAbsensi) {
                if (absensi.getTanggal().equals(workDay)) {
                    found = true;
                    if ("Valid".equals(absensi.getStatus())) {
                        tepatWaktu++;
                    } else if ("Invalid".equals(absensi.getStatus())) {
                        terlambat++;
                    } else if ("Pending".equals(absensi.getStatus())) {
                        // Jika masih pending, tidak dihitung sebagai tidak masuk
                        // karena masih dalam proses absensi
                    } else if ("Belum Lengkap".equals(absensi.getStatus()) && workDay.isBefore(DateTimeUtil.getCurrentDateWIB())) {
                        tidakMasuk++;
                    }
                    break;
                }
            }
            
            // Jika tidak ada absensi untuk hari kerja dan hari tersebut sudah lewat, hitung sebagai tidak masuk
            if (!found && workDay.isBefore(DateTimeUtil.getCurrentDateWIB())) {
                tidakMasuk++;
            }
        }
        
        // Membuat response
        return LaporanKehadiranUserResponse.builder()
                .idUser(userProfile.getId_user())
                .namaUser(userProfile.getFirstname() + " " + userProfile.getLastname())
                .bidangKerja(userProfile.getBidangKerja())
                .periode("Minggu ke-" + weekNumber)
                .tepatWaktu(tepatWaktu)
                .terlambat(terlambat)
                .tidakMasuk(tidakMasuk)
                .izin(izin)
                .build();
    }
    
    /**
     * Mendapatkan laporan kehadiran user berdasarkan bulan
     * @param idUser ID user yang login
     * @param month Bulan (1-12)
     * @param year Tahun
     * @return LaporanKehadiranUserResponse
     */
    public LaporanKehadiranUserResponse getLaporanKehadiranUserByMonth(Integer idUser, int month, int year) {
        logger.info("Mengambil laporan kehadiran user ID {} untuk bulan {} tahun {}", idUser, month, year);
        
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return null;
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Menentukan tanggal awal dan akhir bulan
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        
        // Mendapatkan absensi user dalam rentang tanggal
        List<Absensi> userAbsensi = absensiRepository.findByUserProfileAndDateRange(userProfile, startDate, endDate);
        
        // Menghitung jumlah berdasarkan status
        int tepatWaktu = 0;
        int terlambat = 0;
        int tidakMasuk = 0;
        int izin = 0; // Jika ada fitur izin
        
        // Menghitung jumlah hari kerja dalam rentang tanggal (Senin-Jumat)
        List<LocalDate> workDays = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // Memeriksa status absensi untuk setiap hari kerja
        for (LocalDate workDay : workDays) {
            boolean found = false;
            for (Absensi absensi : userAbsensi) {
                if (absensi.getTanggal().equals(workDay)) {
                    found = true;
                    if ("Valid".equals(absensi.getStatus())) {
                        tepatWaktu++;
                    } else if ("Invalid".equals(absensi.getStatus())) {
                        terlambat++;
                    } else if ("Pending".equals(absensi.getStatus())) {
                        // Jika masih pending, tidak dihitung sebagai tidak masuk
                        // karena masih dalam proses absensi
                    } else if ("Belum Lengkap".equals(absensi.getStatus()) && workDay.isBefore(DateTimeUtil.getCurrentDateWIB())) {
                        tidakMasuk++;
                    }
                    break;
                }
            }
            
            // Jika tidak ada absensi untuk hari kerja dan hari tersebut sudah lewat, hitung sebagai tidak masuk
            if (!found && workDay.isBefore(DateTimeUtil.getCurrentDateWIB())) {
                tidakMasuk++;
            }
        }
        
        // Membuat response
        String namaBulan = Month.of(month).toString();
        return LaporanKehadiranUserResponse.builder()
                .idUser(userProfile.getId_user())
                .namaUser(userProfile.getFirstname() + " " + userProfile.getLastname())
                .bidangKerja(userProfile.getBidangKerja())
                .periode("Bulan " + namaBulan)
                .tepatWaktu(tepatWaktu)
                .terlambat(terlambat)
                .tidakMasuk(tidakMasuk)
                .izin(izin)
                .build();
    }
}

// Tambahkan scheduled task untuk memperbarui status absensi pada jam 21:00 setiap hari
