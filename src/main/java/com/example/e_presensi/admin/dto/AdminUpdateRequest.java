package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password; // Opsional, hanya diubah jika tidak null/empty
    private String phoneNumber;
    private String alamat;
    private String status;
}