package com.dwish.minitrxsettlement.repository;

import com.dwish.minitrxsettlement.entity.Transaction;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends ListCrudRepository<Transaction, UUID> {
}
