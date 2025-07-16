package com.vpbankhackathon.t24_service.controllers;

import com.vpbankhackathon.t24_service.models.dtos.AccountOpeningRequestDTO;
import com.vpbankhackathon.t24_service.models.dtos.ResponseDTO;
import com.vpbankhackathon.t24_service.models.dtos.TransactionRequestDTO;
import com.vpbankhackathon.t24_service.services.DataSourceAndIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/t24")
public class T24Controller {

    @Autowired
    private DataSourceAndIntegrationService service;

    @PostMapping("/transactions/transfer")
    public ResponseEntity<ResponseDTO> transfer(@RequestBody TransactionRequestDTO transactionRequestDTO) {
        // Logic for transfer transaction
        System.out.println("Transfer transaction initiated.");
        return ResponseEntity.ok(service.processTransfer(transactionRequestDTO));
    }

    @PostMapping("/accounts/open")
    public ResponseEntity<ResponseDTO> openAccount(@RequestBody AccountOpeningRequestDTO accountOpeningRequestDTO) {
        // Logic for opening an account
        service.openAccount(accountOpeningRequestDTO);
        System.out.println("Account opening initiated.");
        return ResponseEntity.ok(
            new ResponseDTO(
                "success",
                "Transfer completed successfully",
                ""
            )
        );
    }
}
