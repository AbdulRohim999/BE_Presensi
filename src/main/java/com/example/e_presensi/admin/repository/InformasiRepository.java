package com.example.e_presensi.admin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_presensi.admin.model.Informasi;

@Repository
public interface InformasiRepository extends JpaRepository<Informasi, Integer> {
    @Query("SELECT i FROM Informasi i WHERE i.tanggalMulai <= :today AND i.tanggalSelesai >= :today ORDER BY i.createdAt DESC")
    List<Informasi> findActiveInformasi(@Param("today") LocalDate today);

    List<Informasi> findAllByOrderByCreatedAtDesc();

    List<Informasi> findByJudulContainingIgnoreCaseOrderByCreatedAtDesc(String judul);
    
    // Mencari informasi aktif berdasarkan target tipe user
    @Query("SELECT i FROM Informasi i WHERE i.tanggalMulai <= :today AND i.tanggalSelesai >= :today AND (i.targetTipeUser = :tipeUser OR i.targetTipeUser = 'semua') ORDER BY i.createdAt DESC")
    List<Informasi> findActiveInformasiByTipeUser(@Param("today") LocalDate today, @Param("tipeUser") String tipeUser);

    // Mencari semua informasi (aktif dan non-aktif) berdasarkan target tipe user
    @Query("SELECT i FROM Informasi i WHERE i.targetTipeUser = :tipeUser OR i.targetTipeUser = 'semua' ORDER BY i.createdAt DESC")
    List<Informasi> findAllByTipeUser(@Param("tipeUser") String tipeUser);
} 