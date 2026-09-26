package com.dwish.minitrxsettlement.mapper;

import com.dwish.minitrxsettlement.dto.AccountCreateRequest;
import com.dwish.minitrxsettlement.dto.AccountResponse;
import com.dwish.minitrxsettlement.entity.Account;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
public class AccountMapper {

    public static AccountResponse toResponse(Account account) {
        if (account == null) return null;

        AccountResponse result = new AccountResponse();
        result.setId(account.getId());
        result.setName(account.getName());
        result.setCif(account.getCif());
        result.setBalance(account.getBalance());
        result.setCreatedAt(account.getCreatedAt());

        return result;
    }

    public static Account toEntityCreate(AccountCreateRequest account) {
        Account result = new Account();
        result.setName(account.name());
        result.setCif(account.cif());
        result.setCreatedAt(Instant.now());
        if (account.initialBalance() != null) {
            result.setBalance(account.initialBalance());
        }
        else {
            result.setBalance(new BigDecimal(0));
        }

        return result;
    }
}
