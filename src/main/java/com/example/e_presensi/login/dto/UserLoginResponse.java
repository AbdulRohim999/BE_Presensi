package com.example.e_presensi.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {
    private Integer id_user;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private String tipe_user;
    private String status;
    private String token;
}