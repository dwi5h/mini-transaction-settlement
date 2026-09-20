package com.dwish.minitrxsettlement.mapper;

import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.entity.Transaction;
import lombok.NoArgsConstructor;

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
}
