package com.dwish.minitrxsettlement.mapper;

import com.dwish.minitrxsettlement.dto.TransactionCreateRequest;
import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.entity.Transaction;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        TransactionResponse result = new TransactionResponse();
        result.setId(transaction.getId());
        result.setAccountId(transaction.getAccountId());
        result.setType(transaction.getType());
        result.setAmount(transaction.getAmount());
        result.setStatus(transaction.getStatus());
        result.setCreatedAt(transaction.getCreatedAt());
        result.setProcessedAt(transaction.getProcessedAt());

        return result;
    }

    public static Transaction createPending(TransactionCreateRequest request) {
        if (request == null) return null;

        Transaction result = new Transaction();
        result.setId(UUID.randomUUID());
        result.setAccountId(request.accountId());
        result.setType(request.type());
        result.setAmount(request.amount());
        result.setStatus("PENDING");
        result.setCreatedAt(Instant.now());
        result.setProcessedAt(null);

        return result;
    }
}
