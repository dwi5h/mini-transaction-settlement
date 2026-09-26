package com.dwish.minitrxsettlement;

import com.dwish.minitrxsettlement.dto.AccountCreateRequest;
import com.dwish.minitrxsettlement.dto.AccountResponse;
import com.dwish.minitrxsettlement.entity.Account;
import com.dwish.minitrxsettlement.exception.ResourceNotFoundException;
import com.dwish.minitrxsettlement.repository.AccountRepository;
import com.dwish.minitrxsettlement.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void create_withInitialBalance_usesProvidedBalance() {
        AccountCreateRequest request = new AccountCreateRequest("Dwi Septihadi", new BigDecimal("100000"), "CIF00123");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.create(request);

        assertEquals(0, response.getBalance().compareTo(new BigDecimal("100000")));
    }

    @Test
    void create_withoutInitialBalance_defaultsToZero() {
        AccountCreateRequest request = new AccountCreateRequest("Dwi Septihadi", null, "CIF00123");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.create(request);

        assertEquals(0, response.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void getAccountById_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(id));
    }
}
