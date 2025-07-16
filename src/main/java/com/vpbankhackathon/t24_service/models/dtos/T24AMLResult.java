package com.vpbankhackathon.t24_service.models.dtos;

import lombok.Data;

@Data
public class T24AMLResult {
    private String type; // CUSTOMER or TRANSACTION
    private Long id;
    private String status; // CLEAR or SUSPENDED or SUSPICIOUS
    private String reason;
}

