package com.vpbankhackathon.t24_service.models.dtos;

import lombok.Data;

@Data
public class VerifyCustomerRequestDTO {
    private String transactionId;
    private String customerId;
    private String customerName; // Tên khách hàng
    private String customerIdentificationNumber; // Số định danh (CCCD/CMND/Hộ chiếu)
}
