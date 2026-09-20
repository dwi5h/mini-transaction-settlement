package com.dwish.minitrxsettlement.service;

import com.dwish.minitrxsettlement.dto.PaggingRequest;
import com.dwish.minitrxsettlement.dto.AccountResponse;
import com.dwish.minitrxsettlement.exception.ResourceNotFoundException;
import com.dwish.minitrxsettlement.mapper.AccountMapper;
import com.dwish.minitrxsettlement.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Page<AccountResponse> getAllAccounts(PaggingRequest request) {
        String sortBy = "createdAt";
        Sort sort = request.direction().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(request.getJpaPage(), request.size(), sort);

        return accountRepository.findAll(pageable)
                .map(AccountMapper::toResponse);
    }

    public AccountResponse getAccountById(UUID id) {
        return accountRepository.findById(id)
                .map(AccountMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Account dengan ID " + id + " tidak ditemukan"));
    }
}
