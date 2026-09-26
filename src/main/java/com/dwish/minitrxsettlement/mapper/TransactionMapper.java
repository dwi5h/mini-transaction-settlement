package com.dwish.minitrxsettlement.mapper;

import com.dwish.minitrxsettlement.dto.TransactionCreateRequest;
import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.dto.TransactionStatus;
import com.dwish.minitrxsettlement.dto.TransactionType;
import com.dwish.minitrxsettlement.entity.Account;
import com.dwish.minitrxsettlement.entity.Transaction;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        TransactionResponse result = new TransactionResponse();
        result.setId(transaction.getId());
        result.setAccountId(transaction.getAccount().getId());
        result.setType(transaction.getType());
        result.setAmount(transaction.getAmount());
        result.setStatus(transaction.getStatus());
        result.setCreatedAt(transaction.getCreatedAt());
        result.setProcessedAt(transaction.getProcessedAt());

        return result;
    }

    public static Transaction createPending(TransactionCreateRequest request, Account account) {
        if (request == null) return null;

        Transaction result = new Transaction();
        result.setAccount(account);
        TransactionType type = TransactionType.valueOf(request.type().toUpperCase());
        result.setType(type);
        result.setAmount(request.amount());
        result.setStatus(TransactionStatus.PENDING);
        result.setCreatedAt(Instant.now());
        result.setProcessedAt(null);
        return result;
    }
}
