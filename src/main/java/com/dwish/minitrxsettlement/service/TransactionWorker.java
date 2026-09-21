package com.dwish.minitrxsettlement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionWorker {

    private final TransactionService transactionService;

    @KafkaListener(topics = "txn-requested", groupId = "txn-worker")
    public void onMessage(String transactionId) {
        transactionService.settle(UUID.fromString(transactionId));
    }
}
