package com.vpbankhackathon.t24_service.models.dtos;

import lombok.Data;

@Data
public class ResponseDTO {
    private String status;
    private String message;
    private String transactionId;

    public ResponseDTO(String status, String message, String transactionId) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
    }

    public ResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
        this.transactionId = null;
    }

    public ResponseDTO() {

    }
}
