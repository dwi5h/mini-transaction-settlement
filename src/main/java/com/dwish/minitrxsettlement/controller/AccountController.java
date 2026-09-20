package com.dwish.minitrxsettlement.controller;


import com.dwish.minitrxsettlement.dto.PaggingRequest;
import com.dwish.minitrxsettlement.dto.AccountResponse;
import com.dwish.minitrxsettlement.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        log.info("REST request untuk mendapatkan Account berdasarkan ID: {}", id);

        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(@Valid PaggingRequest request) {
        log.info("REST request untuk mendapatkan semua produk. Halaman: {}, Ukuran: {}, ({})",
                request.page(), request.size(), request.direction());

        Page<AccountResponse> accounts = accountService.getAllAccounts(request);

        return ResponseEntity.ok(accounts);
    }
}
