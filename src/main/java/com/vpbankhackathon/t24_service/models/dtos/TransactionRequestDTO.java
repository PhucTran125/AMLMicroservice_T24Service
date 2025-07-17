package com.vpbankhackathon.t24_service.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TransactionRequestDTO {
    private Long amount; // Số tiền
    private String currency; // Loại tiền (e.g., VND, USD)
    private Long sourceAccountNumber; // Tài khoản nguồn
    private Long destinationAccountNumber; // Tài khoản đích
    private Long customerId; // Mã khách hàng
    private String customerName; // Tên khách hàng
    private String customerIdentificationNumber; // Số định danh (CCCD/CMND/Hộ chiếu)
    private String country; // Quốc gia
    private String date;
}
