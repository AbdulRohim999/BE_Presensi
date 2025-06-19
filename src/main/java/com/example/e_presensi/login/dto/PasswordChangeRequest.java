package com.example.e_presensi.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {
    @NotBlank(message = "Password lama tidak boleh kosong")
    private String oldPassword;
    
    @NotBlank(message = "Password baru tidak boleh kosong")
    @Size(min = 8, message = "Password baru harus minimal 8 karakter")
    private String newPassword;
    
    @NotBlank(message = "Konfirmasi password tidak boleh kosong")
    private String confirmPassword;
    
    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}