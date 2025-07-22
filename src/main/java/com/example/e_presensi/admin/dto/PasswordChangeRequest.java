package com.example.e_presensi.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    
    @NotBlank(message = "Password baru tidak boleh kosong")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String newPassword;
    
    @NotBlank(message = "Konfirmasi password tidak boleh kosong")
    private String confirmPassword;
} 