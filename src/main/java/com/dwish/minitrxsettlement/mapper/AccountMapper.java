package com.dwish.minitrxsettlement.mapper;

import com.dwish.minitrxsettlement.dto.AccountResponse;
import com.dwish.minitrxsettlement.entity.Account;
import lombok.NoArgsConstructor;

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
}
