package com.example.e_presensi.user.repository;

import com.example.e_presensi.login.model.UserProfile;
import com.example.e_presensi.user.model.Absensi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsensiRepository extends JpaRepository<Absensi, Integer> {
    
    Optional<Absensi> findByUserProfileAndTanggal(UserProfile userProfile, LocalDate tanggal);
    
    List<Absensi> findByUserProfileOrderByTanggalDesc(UserProfile userProfile);
    
    @Query("SELECT a FROM Absensi a WHERE a.userProfile = :userProfile AND a.tanggal BETWEEN :startDate AND :endDate ORDER BY a.tanggal DESC")
    List<Absensi> findByUserProfileAndDateRange(@Param("userProfile") UserProfile userProfile, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    // Tambahkan method untuk mencari absensi berdasarkan tanggal
    List<Absensi> findByTanggal(LocalDate tanggal);
    
    // Tambahkan method untuk mencari absensi berdasarkan range tanggal
    List<Absensi> findByTanggalBetween(LocalDate tanggalMulai, LocalDate tanggalSelesai);
}