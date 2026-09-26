package com.dwish.minitrxsettlement;

import com.dwish.minitrxsettlement.dto.TransactionCreateRequest;
import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.dto.TransactionStatus;
import com.dwish.minitrxsettlement.dto.TransactionType;
import com.dwish.minitrxsettlement.entity.Account;
import com.dwish.minitrxsettlement.entity.Transaction;
import com.dwish.minitrxsettlement.exception.ResourceNotFoundException;
import com.dwish.minitrxsettlement.repository.AccountRepository;
import com.dwish.minitrxsettlement.repository.TransactionRepository;
import com.dwish.minitrxsettlement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionService}, focused on the async settlement
 * logic in settle() — the core business rule of the project: balance
 * validation, idempotency against duplicate Kafka delivery, and the
 * PENDING -> PROCESSED/FAILED state transition.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID accountId;
    private UUID transactionId;
    private Account account;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        account = new Account();
        account.setId(accountId);
        account.setName("Dwi Septihadi");
        account.setCif("CIF00123");
        account.setBalance(new BigDecimal("100000"));
        account.setCreatedAt(Instant.now());

        transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal("50000"));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(Instant.now());
    }

    // ---------- settle() ----------

    @Test
    void settle_debitWithSufficientBalance_marksProcessedAndDeductsBalance() {
        transaction.setType(TransactionType.DEBIT);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        transactionService.settle(transactionId);

        assertEquals(TransactionStatus.PROCESSED, transaction.getStatus());
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("50000")));
        assertNotNull(transaction.getProcessedAt());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void settle_debitWithInsufficientBalance_marksFailedAndLeavesBalanceUnchanged() {
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("500000")); // exceeds balance of 100000
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        transactionService.settle(transactionId);

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("100000"))); // untouched
        assertNotNull(transaction.getProcessedAt());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void settle_credit_alwaysProcessedAndIncreasesBalance() {
        transaction.setType(TransactionType.CREDIT);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        transactionService.settle(transactionId);

        assertEquals(TransactionStatus.PROCESSED, transaction.getStatus());
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("150000")));
        verify(accountRepository).save(account);
    }

    @Test
    void settle_alreadyProcessedTransaction_isIdempotentAndSkipsReprocessing() {
        // Simulates Kafka delivering the same message twice.
        transaction.setType(TransactionType.DEBIT);
        transaction.setStatus(TransactionStatus.PROCESSED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        transactionService.settle(transactionId);

        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void settle_transactionNotFound_throwsNoSuchElementException() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> transactionService.settle(transactionId));
    }

    @Test
    void settle_accountNotFound_throwsNoSuchElementException() {
        transaction.setType(TransactionType.DEBIT);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> transactionService.settle(transactionId));
    }

    // ---------- createPending() ----------

    @Test
    void createPending_validRequest_savesTransactionAsPending() {
        TransactionCreateRequest request =
                new TransactionCreateRequest(accountId.toString(), new BigDecimal("25000"), "CREDIT");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.createPending(request);

        assertEquals(TransactionStatus.PENDING, response.getStatus());
        assertEquals(TransactionType.CREDIT, response.getType());
        assertEquals(0, response.getAmount().compareTo(new BigDecimal("25000")));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createPending_accountNotFound_throwsResourceNotFoundException() {
        TransactionCreateRequest request =
                new TransactionCreateRequest(accountId.toString(), new BigDecimal("25000"), "CREDIT");
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createPending(request));
    }
}
