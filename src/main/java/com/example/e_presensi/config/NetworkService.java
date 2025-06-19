package com.example.e_presensi.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Ganti dengan import jakarta

@Service
public class NetworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);
    
    // Daftar subnet jaringan kampus (dapat dikonfigurasi di application.properties)
    @Value("${campus.network.subnets:192.168.123.0/23}")
    private List<String> campusSubnets;
    
    private List<SubnetInfo> parsedSubnets = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Parse subnet saat aplikasi dimulai
        for (String subnet : campusSubnets) {
            try {
                parsedSubnets.add(parseSubnet(subnet));
                logger.info("Subnet kampus terdaftar: {}", subnet);
            } catch (Exception e) {
                logger.error("Gagal parsing subnet: {}", subnet, e);
            }
        }
    }
    
    /**
     * Memeriksa apakah IP termasuk dalam jaringan kampus
     */
    public boolean isInCampusNetwork(String ipAddress) {
        try {
            // Log semua informasi IP yang diterima
            logger.info("Memeriksa IP: {}", ipAddress);
            
            // Jika IP adalah localhost, coba dapatkan IP asli
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
            
            InetAddress addr = InetAddress.getByName(ipAddress);
            byte[] ipBytes = addr.getAddress();
            
            // Log informasi IP yang akan diperiksa
            logger.info("Memeriksa IP {} (bytes: {})", ipAddress, bytesToHex(ipBytes));
            
            // Jika dalam mode development, selalu return true
            if (isDevelopmentMode()) {
                logger.info("Mode development: IP {} dianggap dalam jaringan kampus", ipAddress);
                return true;
            }
            
            // Periksa apakah IP termasuk dalam salah satu subnet kampus
            for (SubnetInfo subnet : parsedSubnets) {
                logger.info("Memeriksa subnet: {}", subnet.cidrNotation);
                if (isInSubnet(ipBytes, subnet)) {
                    logger.info("IP {} termasuk dalam jaringan kampus ({})", ipAddress, subnet.cidrNotation);
                    return true;
                }
            }
            
            logger.info("IP {} tidak termasuk dalam jaringan kampus", ipAddress);
            return false;
        } catch (UnknownHostException e) {
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
    
    /**
     * Memeriksa apakah IP termasuk dalam subnet
     */
    private boolean isInSubnet(byte[] ip, SubnetInfo subnet) {
        if (ip.length != subnet.networkAddress.length) {
            return false;
        }
        
        for (int i = 0; i < ip.length; i++) {
            if ((ip[i] & subnet.networkMask[i]) != (subnet.networkAddress[i] & subnet.networkMask[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Parse subnet dalam format CIDR (misalnya 192.168.1.0/24)
     */
    private SubnetInfo parseSubnet(String cidrNotation) throws UnknownHostException {
        String[] parts = cidrNotation.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format CIDR tidak valid: " + cidrNotation);
        }
        
        String ipPart = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);
        
        InetAddress inetAddress = InetAddress.getByName(ipPart);
        byte[] networkAddress = inetAddress.getAddress();
        byte[] networkMask = createNetworkMask(prefixLength, networkAddress.length);
        
        return new SubnetInfo(cidrNotation, networkAddress, networkMask);
    }
    
    /**
     * Membuat network mask berdasarkan prefix length
     */
    private byte[] createNetworkMask(int prefixLength, int length) {
        byte[] mask = new byte[length];
        for (int i = 0; i < length; i++) {
            int bitsToCopy = Math.min(8, prefixLength - (i * 8));
            if (bitsToCopy > 0) {
                mask[i] = (byte) (0xFF << (8 - bitsToCopy));
            } else {
                mask[i] = 0;
            }
        }
        return mask;
    }
    
    // Helper method untuk mengkonversi byte array ke hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    /**
     * Kelas untuk menyimpan informasi subnet
     */
    private static class SubnetInfo {
        private final String cidrNotation;
        private final byte[] networkAddress;
        private final byte[] networkMask;
        
        public SubnetInfo(String cidrNotation, byte[] networkAddress, byte[] networkMask) {
            this.cidrNotation = cidrNotation;
            this.networkAddress = networkAddress;
            this.networkMask = networkMask;
        }
    }
}