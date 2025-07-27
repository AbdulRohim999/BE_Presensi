package com.example.e_presensi.admin.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.e_presensi.admin.dto.KehadiranUserResponse;
import com.example.e_presensi.admin.dto.UserAbsensiStatusResponse;
import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.login.repository.UserProfileRepository;
import com.example.e_presensi.user.dto.LaporanKehadiranUserResponse;
import com.example.e_presensi.user.model.Absensi;
import com.example.e_presensi.user.repository.AbsensiRepository;
import com.example.e_presensi.util.DateTimeUtil;

@Service
public class KehadiranService {
    
    private static final Logger logger = LoggerFactory.getLogger(KehadiranService.class);
    
    @Autowired
    private AbsensiRepository absensiRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    // Mendapatkan kehadiran user hari ini
    public List<KehadiranUserResponse> getKehadiranUserHariIni() {
        LocalDate hariIni = DateTimeUtil.getCurrentDateWIB();
        List<Absensi> absensiList = absensiRepository.findByTanggal(hariIni);
        
        return absensiList.stream()
                .filter(absensi -> {
                    String role = absensi.getUserProfile().getRole();
                    return role != null && role.equals("user"); // Hanya tampilkan user dengan role "user"
                })
                .map(this::mapToKehadiranUserResponse)
                .collect(Collectors.toList());
    }
    
    // Mapping dari Absensi ke KehadiranUserResponse
    private KehadiranUserResponse mapToKehadiranUserResponse(Absensi absensi) {
        UserProfile userProfile = absensi.getUserProfile();
        
        // Mengambil tanggal absensi
        LocalDate tanggalAbsensi = absensi.getTanggal();
        
        // Mengambil hanya waktu (jam) dari timestamp absensi
        LocalTime waktuAbsenPagi = absensi.getAbsenPagi() != null ? absensi.getAbsenPagi().toLocalTime() : null;
        LocalTime waktuAbsenSiang = absensi.getAbsenSiang() != null ? absensi.getAbsenSiang().toLocalTime() : null;
        LocalTime waktuAbsenSore = absensi.getAbsenSore() != null ? absensi.getAbsenSore().toLocalTime() : null;
        
        KehadiranUserResponse response = KehadiranUserResponse.builder()
                .idUser(userProfile.getId_user())
                .namaUser(userProfile.getFirstname() + " " + userProfile.getLastname())
                .bidangKerja(userProfile.getBidangKerja())
                .role(userProfile.getRole()) // Menambahkan role dari userProfile
                .absenPagi(waktuAbsenPagi)
                .absenSiang(waktuAbsenSiang)
                .absenSore(waktuAbsenSore)
                .status(absensi.getStatus())
                .build();
        
        // Mengisi tanggalFormatted dan hari
        response.setTanggalAbsensi(tanggalAbsensi);
        
        return response;
    }
    
    // Mendapatkan kehadiran user berdasarkan ID user
    public List<KehadiranUserResponse> getKehadiranByUserId(Integer idUser) {
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return List.of();
        }
        
        UserProfile userProfile = userProfileOpt.get();
        
        // Verifica si el usuario tiene el rol "user"
        if (userProfile.getRole() == null || !userProfile.getRole().equals("user")) {
            logger.warn("User dengan ID {} bukan user biasa (role: {})", idUser, userProfile.getRole());
            return List.of();
        }
        
        List<Absensi> absensiList = absensiRepository.findByUserProfileOrderByTanggalDesc(userProfile);
        
        return absensiList.stream()
                .map(this::mapToKehadiranUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Mendapatkan laporan kehadiran user berdasarkan minggu
     * @param weekNumber Nomor minggu (1-5)
     * @param month Bulan (1-12)
     * @param year Tahun
     * @return List of LaporanKehadiranUserResponse
     */
    public List<LaporanKehadiranUserResponse> getLaporanKehadiranUserByWeek(int weekNumber, int month, int year) {
        logger.info("Mengambil laporan kehadiran user untuk minggu ke-{} bulan {} tahun {}", weekNumber, month, year);
        
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
            return new ArrayList<>();
        }
        
        // Menentukan tanggal akhir minggu (6 hari setelah tanggal awal atau akhir bulan, mana yang lebih dulu)
        LocalDate endDate = startDate.plusDays(6);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        if (endDate.isAfter(lastDayOfMonth)) {
            endDate = lastDayOfMonth;
        }
        
        // Mendapatkan semua user
        List<UserProfile> allUsers = userProfileRepository.findAll();
        
        // Mendapatkan semua absensi dalam rentang tanggal
        List<Absensi> allAbsensi = absensiRepository.findByTanggalBetween(startDate, endDate);
        
        // Mengelompokkan absensi berdasarkan user
        Map<Integer, List<Absensi>> absensiByUser = allAbsensi.stream()
                .collect(Collectors.groupingBy(a -> a.getUserProfile().getId_user()));
        
        // Membuat laporan untuk setiap user
        List<LaporanKehadiranUserResponse> result = new ArrayList<>();
        for (UserProfile user : allUsers) {
            // Skip jika bukan user biasa (admin atau super_admin)
            if (user.getRole() != null && (user.getRole().equals("admin") || user.getRole().equals("super_admin"))) {
                continue;
            }
            
            // Mendapatkan absensi user
            List<Absensi> userAbsensi = absensiByUser.getOrDefault(user.getId_user(), new ArrayList<>());
            
            // Menghitung jumlah berdasarkan status dari data absensi yang ada
            int tepatWaktu = 0;
            int terlambat = 0;
            int tidakMasuk = 0;
            int izin = 0; // Jika ada fitur izin
            
            // Menghitung jumlah hari kerja dalam rentang tanggal (Senin-Sabtu)
            List<LocalDate> workDays = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                if (dayOfWeek != DayOfWeek.SUNDAY) {
                    workDays.add(currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }
            
            // Menghitung berdasarkan data absensi yang ada
            for (Absensi absensi : userAbsensi) {
                if ("Valid".equalsIgnoreCase(absensi.getStatus())) {
                    tepatWaktu++;
                } else if ("Invalid".equalsIgnoreCase(absensi.getStatus())) {
                    terlambat++;
                } else if ("Pending".equalsIgnoreCase(absensi.getStatus())) {
                    // Jika masih pending, tidak dihitung sebagai tidak masuk
                    // karena masih dalam proses absensi
                }
                // Status "Belum Lengkap" sudah tidak digunakan lagi
            }
            
            // Menghitung hari kerja yang tidak ada absensinya
            for (LocalDate workDay : workDays) {
                boolean found = false;
                for (Absensi absensi : userAbsensi) {
                    if (absensi.getTanggal().equals(workDay)) {
                        found = true;
                        break;
                    }
                }
                
                // Jika tidak ada absensi untuk hari kerja dan hari tersebut sudah lewat, hitung sebagai tidak masuk
                if (!found && workDay.isBefore(DateTimeUtil.getCurrentDateWIB())) {
                    tidakMasuk++;
                }
            }
            
            // Membuat response
            LaporanKehadiranUserResponse laporan = LaporanKehadiranUserResponse.builder()
                    .idUser(user.getId_user())
                    .namaUser(user.getFirstname() + " " + user.getLastname())
                    .bidangKerja(user.getBidangKerja())
                    .periode("Minggu ke-" + weekNumber)
                    .tepatWaktu(tepatWaktu)
                    .terlambat(terlambat)
                    .tidakMasuk(tidakMasuk)
                    .izin(izin)
                    .build();
            
            // Mengisi tanggalLaporan dan hariLaporan dengan tanggal saat ini
            laporan.setTanggalLaporan(DateTimeUtil.getCurrentDateWIB());
            
            result.add(laporan);
        }
        
        return result;
    }
    
    /**
     * Mendapatkan laporan kehadiran user berdasarkan bulan
     * @param month Bulan (1-12)
     * @param year Tahun
     * @return List of LaporanKehadiranUserResponse
     */
    public List<LaporanKehadiranUserResponse> getLaporanKehadiranUserByMonth(int month, int year) {
        logger.info("Mengambil laporan kehadiran user untuk bulan {} tahun {}", month, year);
        
        // Menentukan tanggal awal dan akhir bulan
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        
        // Mendapatkan semua user
        List<UserProfile> allUsers = userProfileRepository.findAll();
        
        // Mendapatkan semua absensi dalam rentang tanggal
        List<Absensi> allAbsensi = absensiRepository.findByTanggalBetween(startDate, endDate);
        
        // Mengelompokkan absensi berdasarkan user
        Map<Integer, List<Absensi>> absensiByUser = allAbsensi.stream()
                .collect(Collectors.groupingBy(a -> a.getUserProfile().getId_user()));
        
        // Membuat laporan untuk setiap user
        List<LaporanKehadiranUserResponse> result = new ArrayList<>();
        for (UserProfile user : allUsers) {
            // Skip jika bukan user biasa (admin atau super_admin)
            if (user.getRole() != null && (user.getRole().equals("admin") || user.getRole().equals("super_admin"))) {
                continue;
            }
            
            // Mendapatkan absensi user
            List<Absensi> userAbsensi = absensiByUser.getOrDefault(user.getId_user(), new ArrayList<>());
            
            // Menghitung jumlah berdasarkan status dari data absensi yang ada
            int tepatWaktu = 0;
            int terlambat = 0;
            int tidakMasuk = 0;
            int izin = 0; // Jika ada fitur izin
            
            // Menghitung jumlah hari kerja dalam rentang tanggal (Senin-Sabtu)
            List<LocalDate> workDays = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                if (dayOfWeek != DayOfWeek.SUNDAY) {
                    workDays.add(currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }
            
            // Menghitung berdasarkan data absensi yang ada
            for (Absensi absensi : userAbsensi) {
                if ("Valid".equalsIgnoreCase(absensi.getStatus())) {
                    tepatWaktu++;
                } else if ("Invalid".equalsIgnoreCase(absensi.getStatus())) {
                    terlambat++;
                } else if ("Pending".equalsIgnoreCase(absensi.getStatus())) {
                    // Jika masih pending, tidak dihitung sebagai tidak masuk
                    // karena masih dalam proses absensi
                }
                // Status "Belum Lengkap" sudah tidak digunakan lagi
            }
            
            // Menghitung hari kerja yang tidak ada absensinya
            for (LocalDate workDay : workDays) {
                boolean found = false;
                for (Absensi absensi : userAbsensi) {
                    if (absensi.getTanggal().equals(workDay)) {
                        found = true;
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
            LaporanKehadiranUserResponse laporan = LaporanKehadiranUserResponse.builder()
                    .idUser(user.getId_user())
                    .namaUser(user.getFirstname() + " " + user.getLastname())
                    .bidangKerja(user.getBidangKerja())
                    .periode("Bulan " + namaBulan)
                    .tepatWaktu(tepatWaktu)
                    .terlambat(terlambat)
                    .tidakMasuk(tidakMasuk)
                    .izin(izin)
                    .build();
            
            // Mengisi tanggalLaporan dan hariLaporan dengan tanggal saat ini
            laporan.setTanggalLaporan(DateTimeUtil.getCurrentDateWIB());
            
            result.add(laporan);
        }
        
        return result;
    }

    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid
     * @return Map berisi jumlah absensi berdasarkan status
     */
    public Map<String, Long> getJumlahAbsensiByStatus() {
        logger.info("Menghitung jumlah absensi berdasarkan status valid, invalid, dan pending");
        
        // Mendapatkan semua data absensi
        List<Absensi> allAbsensi = absensiRepository.findAll();
        
        // Menghitung jumlah berdasarkan status
        long validCount = allAbsensi.stream()
                .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long invalidCount = allAbsensi.stream()
                .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long pendingCount = allAbsensi.stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .count();
        
        // Membuat response
        Map<String, Long> result = new HashMap<>();
        result.put("valid", validCount);
        result.put("invalid", invalidCount);
        result.put("pending", pendingCount);
        result.put("total", (long) allAbsensi.size());
        
        return result;
    }
    
    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk periode tertentu
     * @param startDate Tanggal mulai
     * @param endDate Tanggal selesai
     * @return Map berisi jumlah absensi berdasarkan status
     */
    public Map<String, Long> getJumlahAbsensiByStatusAndPeriode(LocalDate startDate, LocalDate endDate) {
        logger.info("Menghitung jumlah absensi berdasarkan status valid, invalid, dan pending untuk periode {} sampai {}", 
                startDate, endDate);
        
        // Mendapatkan data absensi dalam rentang tanggal
        List<Absensi> absensiList = absensiRepository.findByTanggalBetween(startDate, endDate);
        
        // Menghitung jumlah berdasarkan status
        long validCount = absensiList.stream()
                .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long invalidCount = absensiList.stream()
                .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long pendingCount = absensiList.stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .count();
        
        // Membuat response
        Map<String, Long> result = new HashMap<>();
        result.put("valid", validCount);
        result.put("invalid", invalidCount);
        result.put("pending", pendingCount);
        result.put("total", (long) absensiList.size());
        
        return result;
    }
    
    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk user tertentu
     * @param idUser ID user
     * @return Map berisi jumlah absensi berdasarkan status
     */
    public Map<String, Long> getJumlahAbsensiByStatusAndUserId(Integer idUser) {
        logger.info("Menghitung jumlah absensi berdasarkan status valid, invalid, dan pending untuk user dengan ID {}", idUser);
        
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return Map.of(
                "valid", 0L,
                "invalid", 0L,
                "pending", 0L,
                "total", 0L
            );
        }
        
        UserProfile userProfile = userProfileOpt.get();
        List<Absensi> absensiList = absensiRepository.findByUserProfileOrderByTanggalDesc(userProfile);
        
        // Menghitung jumlah berdasarkan status
        long validCount = absensiList.stream()
                .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long invalidCount = absensiList.stream()
                .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long pendingCount = absensiList.stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .count();
        
        // Membuat response
        Map<String, Long> result = new HashMap<>();
        result.put("valid", validCount);
        result.put("invalid", invalidCount);
        result.put("pending", pendingCount);
        result.put("total", (long) absensiList.size());
        
        return result;
    }
    
    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk user tertentu dan periode tertentu
     * @param idUser ID user
     * @param startDate Tanggal mulai
     * @param endDate Tanggal selesai
     * @return Map berisi jumlah absensi berdasarkan status
     */
    public Map<String, Long> getJumlahAbsensiByStatusAndUserIdAndPeriode(Integer idUser, LocalDate startDate, LocalDate endDate) {
        logger.info("Menghitung jumlah absensi berdasarkan status valid, invalid, dan pending untuk user dengan ID {} pada periode {} sampai {}", 
                idUser, startDate, endDate);
        
        // Cari user profile
        Optional<UserProfile> userProfileOpt = userProfileRepository.findById(idUser);
        if (!userProfileOpt.isPresent()) {
            logger.warn("User dengan ID {} tidak ditemukan", idUser);
            return Map.of(
                "valid", 0L,
                "invalid", 0L,
                "pending", 0L,
                "total", 0L
            );
        }
        
        UserProfile userProfile = userProfileOpt.get();
        List<Absensi> absensiList = absensiRepository.findByUserProfileAndDateRange(userProfile, startDate, endDate);
        
        // Menghitung jumlah berdasarkan status
        long validCount = absensiList.stream()
                .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long invalidCount = absensiList.stream()
                .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                .count();
        
        long pendingCount = absensiList.stream()
                .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                .count();
        
        // Membuat response
        Map<String, Long> result = new HashMap<>();
        result.put("valid", validCount);
        result.put("invalid", invalidCount);
        result.put("pending", pendingCount);
        result.put("total", (long) absensiList.size());
        
        return result;
    }
    
    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk semua user dalam periode tertentu
     * @param startDate Tanggal mulai
     * @param endDate Tanggal selesai
     * @return List berisi informasi user dan jumlah absensi berdasarkan status dalam periode tertentu
     */
    public List<UserAbsensiStatusResponse> getJumlahAbsensiByStatusForAllUsersAndPeriode(LocalDate startDate, LocalDate endDate) {
        logger.info("Menghitung jumlah absensi berdasarkan status valid dan invalid untuk semua user dalam periode {} sampai {}", 
                startDate, endDate);
        
        // Mendapatkan semua user
        List<UserProfile> allUsers = userProfileRepository.findAll().stream()
                .filter(user -> "user".equals(user.getRole()))
                .collect(Collectors.toList());
        
        // Mendapatkan semua data absensi dalam rentang tanggal
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Absensi> allAbsensi = absensiRepository.findByTanggalBetween(startDate, endDate);
        
        // Membuat response untuk setiap user
        List<UserAbsensiStatusResponse> result = new ArrayList<>();
        
        // Mendapatkan nama bulan untuk periode
        String periodeName = "Periode " + startDate.toString() + " s/d " + endDate.toString();
        
        for (UserProfile user : allUsers) {
            // Filter absensi untuk user ini
            List<Absensi> userAbsensi = allAbsensi.stream()
                    .filter(a -> a.getUserProfile().getId_user().equals(user.getId_user()))
                    .collect(Collectors.toList());
            
            logger.info("User {} (ID: {}) memiliki {} absensi dalam periode {} sampai {}", 
                    user.getFirstname() + " " + user.getLastname(), 
                    user.getId_user(), 
                    userAbsensi.size(), 
                    startDate, 
                    endDate);
            
            // Debug: Log setiap absensi dan statusnya
            for (Absensi absensi : userAbsensi) {
                logger.info("Absensi tanggal {} untuk user {}: status = {}", 
                        absensi.getTanggal(), 
                        user.getFirstname() + " " + user.getLastname(), 
                        absensi.getStatus());
            }
            
            // Menghitung jumlah berdasarkan status
            long validCount = userAbsensi.stream()
                    .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            long invalidCount = userAbsensi.stream()
                    .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            long pendingCount = userAbsensi.stream()
                    .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            // Menggabungkan "Pending" ke "Invalid"
            invalidCount += pendingCount;
            
            logger.info("User {} - Valid: {}, Invalid: {}, Pending: {}", 
                    user.getFirstname() + " " + user.getLastname(),
                    validCount, invalidCount, pendingCount);
            
            // Membuat response untuk user ini
            UserAbsensiStatusResponse userResponse = UserAbsensiStatusResponse.builder()
                    .idUser(user.getId_user())
                    .namaUser(user.getFirstname() + " " + user.getLastname())
                    .tipeUser(user.getTipeUser())
                    .bidangKerja(user.getBidangKerja())
                    .role(user.getRole()) // Menambahkan role
                    .validCount(validCount)
                    .invalidCount(invalidCount)
                    .totalCount((long) userAbsensi.size())
                    .build();
            
            // Mengisi tanggalLaporan dan hariLaporan dengan tanggal saat ini
            userResponse.setTanggalLaporan(DateTimeUtil.getCurrentDateWIB());
            
            result.add(userResponse);
        }
        
        return result;
    }

    /**
     * Mendapatkan jumlah absensi berdasarkan status valid dan invalid untuk semua user
     * @return List berisi informasi user dan jumlah absensi berdasarkan status
     */
    public List<UserAbsensiStatusResponse> getJumlahAbsensiByStatusForAllUsers() {
        logger.info("Menghitung jumlah absensi berdasarkan status valid dan invalid untuk semua user");
        
        // Mendapatkan semua user
        List<UserProfile> allUsers = userProfileRepository.findAll().stream()
                .filter(user -> "user".equals(user.getRole()))
                .collect(Collectors.toList());
        
        // Mendapatkan semua data absensi
        List<Absensi> allAbsensi = absensiRepository.findAll();
        
        // Membuat response untuk setiap user
        List<UserAbsensiStatusResponse> result = new ArrayList<>();
        
        for (UserProfile user : allUsers) {
            // Filter absensi untuk user ini
            List<Absensi> userAbsensi = allAbsensi.stream()
                    .filter(a -> a.getUserProfile().getId_user().equals(user.getId_user()))
                    .collect(Collectors.toList());
            
            logger.info("User {} (ID: {}) memiliki {} absensi total", 
                    user.getFirstname() + " " + user.getLastname(), 
                    user.getId_user(), 
                    userAbsensi.size());
            
            // Debug: Log setiap absensi dan statusnya
            for (Absensi absensi : userAbsensi) {
                logger.info("Absensi tanggal {} untuk user {}: status = {}", 
                        absensi.getTanggal(), 
                        user.getFirstname() + " " + user.getLastname(), 
                        absensi.getStatus());
            }
            
            // Menghitung jumlah berdasarkan status
            long validCount = userAbsensi.stream()
                    .filter(a -> "Valid".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            long invalidCount = userAbsensi.stream()
                    .filter(a -> "Invalid".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            long pendingCount = userAbsensi.stream()
                    .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
                    .count();
            
            // Menggabungkan "Pending" ke "Invalid"
            invalidCount += pendingCount;
            
            logger.info("User {} - Valid: {}, Invalid: {}, Pending: {}", 
                    user.getFirstname() + " " + user.getLastname(),
                    validCount, invalidCount, pendingCount);
            
            // Membuat response untuk user ini
            UserAbsensiStatusResponse userResponse = UserAbsensiStatusResponse.builder()
                    .idUser(user.getId_user())
                    .namaUser(user.getFirstname() + " " + user.getLastname())
                    .tipeUser(user.getTipeUser())
                    .bidangKerja(user.getBidangKerja())
                    .role(user.getRole()) // Menambahkan role
                    .validCount(validCount)
                    .invalidCount(invalidCount)
                    .totalCount((long) userAbsensi.size())
                    .build();
            
            // Mengisi tanggalLaporan dan hariLaporan dengan tanggal saat ini
            userResponse.setTanggalLaporan(DateTimeUtil.getCurrentDateWIB());
            
            result.add(userResponse);
        }
        
        return result;
    }
}