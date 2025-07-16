package com.vpbankhackathon.t24_service.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AccountOpening {
    @Id
    private Long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // Generate a positive long ID
    private Long timestamp;
    private String customerName;
    private String customerIdentificationNumber;
    private String dob;
    private String nationality;
    private String residentialAddress;
    private String status; // e.g., "pending", "approved", "rejected"
}
