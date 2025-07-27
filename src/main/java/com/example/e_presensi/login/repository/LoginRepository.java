package com.example.e_presensi.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_presensi.login.model.Login;
import com.example.e_presensi.login.model.UserProfile;

public interface LoginRepository extends JpaRepository<Login, Integer> {
    Optional<Login> findByEmail(String email);
    Optional<Login> findByUsername(String username);
    Optional<Login> findByUserProfile(UserProfile userProfile);
}