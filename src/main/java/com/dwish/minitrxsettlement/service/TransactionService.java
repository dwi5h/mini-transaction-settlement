package com.dwish.minitrxsettlement.service;

import com.dwish.minitrxsettlement.dto.*;
import com.dwish.minitrxsettlement.entity.Account;
import com.dwish.minitrxsettlement.entity.Transaction;
import com.dwish.minitrxsettlement.exception.ResourceNotFoundException;
import com.dwish.minitrxsettlement.mapper.TransactionMapper;
import com.dwish.minitrxsettlement.repository.AccountRepository;
import com.dwish.minitrxsettlement.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Page<TransactionResponse> getAllTransactions(PaggingRequest request) {
        String sortBy = "createdAt";
        Sort sort = request.direction().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(request.getJpaPage(), request.size(), sort);

        return transactionRepository.findAll(pageable)
                .map(TransactionMapper::toResponse);
    }

    public TransactionResponse getTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi dengan ID " + id + " tidak ditemukan"));
    }

    @Transactional
    public TransactionResponse createPending(TransactionCreateRequest request) {
        Account account = accountRepository.findById(UUID.fromString(request.accountId()))
                .orElseThrow(() -> new ResourceNotFoundException("Account ID " + request.accountId() + " tidak ditemukan"));

        Transaction transaction = TransactionMapper.createPending(request, account);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public void settle(UUID transactionId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found"));

        if (txn.getStatus() != TransactionStatus.PENDING) {
            return;
        }

        Account account = accountRepository.findByIdForUpdate(txn.getAccount().getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        boolean sufficient = txn.getType() != TransactionType.DEBIT
                || account.getBalance().compareTo(txn.getAmount()) >= 0;

        if (sufficient) {
            BigDecimal delta = txn.getType() == TransactionType.DEBIT
                    ? txn.getAmount().negate()
                    : txn.getAmount();
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);

            txn.setStatus(TransactionStatus.PROCESSED);
        } else {
            txn.setStatus(TransactionStatus.FAILED);
        }

        txn.setProcessedAt(Instant.now());
        transactionRepository.save(txn);
    }
}
