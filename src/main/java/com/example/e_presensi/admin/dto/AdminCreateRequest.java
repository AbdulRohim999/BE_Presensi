package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
}