package com.dwish.minitrxsettlement.service;

import com.dwish.minitrxsettlement.dto.PaggingRequest;
import com.dwish.minitrxsettlement.dto.TransactionResponse;
import com.dwish.minitrxsettlement.exception.ResourceNotFoundException;
import com.dwish.minitrxsettlement.mapper.TransactionMapper;
import com.dwish.minitrxsettlement.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository TransactionRepository;

    public Page<TransactionResponse> getAllTransactions(PaggingRequest request) {
        String sortBy = "createdAt";
        Sort sort = request.direction().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(request.getJpaPage(), request.size(), sort);

        return TransactionRepository.findAll(pageable)
                .map(TransactionMapper::toResponse);
    }

    public TransactionResponse getTransactionById(UUID id) {
        return TransactionRepository.findById(id)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi dengan ID " + id + " tidak ditemukan"));
    }
}
