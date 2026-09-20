package com.dwish.minitrxsettlement.controller;


import com.dwish.minitrxsettlement.dto.TransactionCreateRequest;
import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.dto.PaggingRequest;
import com.dwish.minitrxsettlement.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        log.info("REST request untuk mendapatkan Transaction berdasarkan ID: {}", id);

        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(@Valid PaggingRequest request) {
        log.info("REST request untuk mendapatkan semua produk. Halaman: {}, Ukuran: {}, ({})",
                request.page(), request.size(), request.direction());

        Page<TransactionResponse> transactions = transactionService.getAllTransactions(request);

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionCreateRequest request) {
        log.info("REST request untuk membuat transaksi baru. Account-id: {}",
                request.accountId());

        TransactionResponse response = transactionService.createPending(request);
        // KAFKA SEND HERE

        return ResponseEntity.accepted().body(response);
    }
}
