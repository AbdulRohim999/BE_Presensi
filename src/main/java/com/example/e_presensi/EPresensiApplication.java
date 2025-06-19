package com.example.e_presensi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import com.example.e_presensi.util.MinioUtil;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "E-Presensi API",
        version = "1.0",
        description = "API Documentation untuk E-Presensi"
    )
)
public class EPresensiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EPresensiApplication.class, args);
    }

    // Tambahkan di class EPresensiApplication
    
    @Autowired
    private MinioUtil minioUtil;
    
    @PostConstruct
    public void init() {
        // Inisialisasi bucket MinIO saat aplikasi startup
        minioUtil.initBucket();
    }
}
