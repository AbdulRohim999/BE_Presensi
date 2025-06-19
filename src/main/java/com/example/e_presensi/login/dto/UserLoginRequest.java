package com.example.e_presensi.login.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;
    private String password;
}