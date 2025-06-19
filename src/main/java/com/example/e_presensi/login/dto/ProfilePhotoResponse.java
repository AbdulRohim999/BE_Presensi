package com.example.e_presensi.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePhotoResponse {
    private Integer idUser;
    private String fotoProfile;
    private String fotoProfileUrl; // URL untuk akses foto
}