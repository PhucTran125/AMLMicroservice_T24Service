package com.vpbankhackathon.t24_service.models.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AccountOpeningRequestDTO {
    private Long customerId; // ID khách hàng
    private String customerName; // Tên khách hàng
    private String customerIdentificationNumber; // Số định danh (CCCD/CMND/Hộ chiếu)
    private String dob;
    private String nationality; // Quốc tịch
    private String residentialAddress; // Địa chỉ cư trú
    private String signature; // Chữ ký số
}
