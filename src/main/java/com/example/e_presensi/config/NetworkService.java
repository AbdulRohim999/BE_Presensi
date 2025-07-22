package com.example.e_presensi.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NetworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);
    
    // Daftar IP jaringan kampus (dapat dikonfigurasi di application.properties)
    @Value("${campus.network.ip:110.137.82.92, 110.137.83.234}")
    private String campusIpRaw;
    private List<String> campusIpList;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Split IP berdasarkan koma dan trim spasi
        campusIpList = Arrays.stream(campusIpRaw.split(","))
                .map(String::trim)
                .toList();
    }
    
    public boolean isInCampusNetwork(String ipAddress) {
        try {
            logger.info("Memeriksa IP: {}", ipAddress);
            if (ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("127.0.0.1")) {
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    String hostAddress = localHost.getHostAddress();
                    logger.info("IP localhost terdeteksi, mencoba mendapatkan IP asli: {}", hostAddress);
                    ipAddress = hostAddress;
                } catch (UnknownHostException e) {
                    logger.warn("Tidak dapat mendapatkan IP asli untuk localhost", e);
                }
            }
            // Jika dalam mode development, selalu return true
            if (isDevelopmentMode()) {
                logger.info("Mode development: IP {} dianggap dalam jaringan kampus", ipAddress);
                return true;
            }
            // Bandingkan dengan semua IP kampus
            if (campusIpList.contains(ipAddress)) {
                logger.info("IP {} cocok dengan salah satu IP kampus {}", ipAddress, campusIpList);
                return true;
            }
            logger.info("IP {} tidak cocok dengan daftar IP kampus {}", ipAddress, campusIpList);
            return false;
        } catch (Exception e) {
            logger.error("Error saat memeriksa IP: {}", ipAddress, e);
            return false;
        }
    }
    
    /**
     * Memeriksa apakah aplikasi berjalan dalam mode development
     */
    @Value("${spring.profiles.active:production}")
    private String activeProfile;
    
    private boolean isDevelopmentMode() {
        return "dev".equals(activeProfile) || "development".equals(activeProfile);
    }
    
}