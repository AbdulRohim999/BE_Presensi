package com.example.e_presensi.login.repository;

import com.example.e_presensi.login.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByNip(String nip);
    // Optional<UserProfile> findByUsername(String username);
    
    // Tambahkan method berikut
    List<UserProfile> findByRole(String role);
    boolean existsByEmail(String email);
    
    // Tambahkan method untuk menghitung user berdasarkan tipe_user
    long countByTipeUser(String tipeUser);
}