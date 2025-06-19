package com.example.e_presensi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String nip;
    private String tipeUser;
    private String status;
    private String bidangKerja;
    private String alamat;
    private String phoneNumber;
}