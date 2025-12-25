package com.medina.heritage.patrimoine.dtos.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO pour recevoir les réclamations depuis Kafka.
 * Correspond exactement à la structure JSON envoyée par le système externe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequestDTO {
    
    private String claimId;          // ID unique de la réclamation depuis le système externe
    private UserInfo user;
    private ClaimInfo claim;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;           // Clerk User ID (ex: user_36sUYTLEqPF4kWjVDbeKVUDsvgK)
        private String email;
        private String name;
        private String phone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimInfo {
        private String serviceType;
        private String title;
        private String description;
        private String priority;
        private LocationInfo location;
        private List<AttachmentInfo> attachments;
        private ExtraData extraData;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private String address;
        private Double latitude;
        private Double longitude;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String url;
        private String fileName;
        private String fileType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraData {
        private String qrCode;
        private String patrimoineType;
    }
}
