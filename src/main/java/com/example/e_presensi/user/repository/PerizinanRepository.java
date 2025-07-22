package com.example.e_presensi.user.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_presensi.user.model.Perizinan;

@Repository
public interface PerizinanRepository extends JpaRepository<Perizinan, Integer> {
    
    List<Perizinan> findByIdUser(Integer idUser);
    
    List<Perizinan> findByIdUserAndStatus(Integer idUser, String status);
    
    List<Perizinan> findByStatus(String status);
    
    List<Perizinan> findByTanggalMulaiBetweenOrTanggalSelesaiBetween(
            LocalDate startDate1, LocalDate endDate1,
            LocalDate startDate2, LocalDate endDate2);
    
    boolean existsByIdUserAndStatusAndTanggalMulaiBetweenOrTanggalSelesaiBetween(
            Integer idUser, String status,
            LocalDate startDate1, LocalDate endDate1,
            LocalDate startDate2, LocalDate endDate2);
}