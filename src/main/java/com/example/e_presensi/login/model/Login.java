package com.example.e_presensi.login.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "login")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_login")
    private Integer id_login;
    
    @OneToOne
    @JoinColumn(name = "id_user")
    private UserProfile userProfile;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "create_at")
    private LocalDateTime createAt;
}