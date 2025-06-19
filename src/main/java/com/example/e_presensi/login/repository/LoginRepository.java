package com.example.e_presensi.login.repository;

import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Integer> {
    Optional<Login> findByEmail(String email);
    Optional<Login> findByUserProfile(UserProfile userProfile);
}