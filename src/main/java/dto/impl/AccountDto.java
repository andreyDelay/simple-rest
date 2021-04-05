package dto.impl;

import dto.Dto;
import model.Account;

import java.util.List;
import java.util.stream.Collectors;

public class AccountDto implements Dto<Account> {

    @Override
    public Account get(Account account) {
        return transferAccount(account);
    }

    @Override
    public List<Account> getAll(List<Account> list) {
        return list.stream()
                    .map(this::transferAccount)
                    .collect(Collectors.toList());
    }

    private Account transferAccount(Account account) {
        if (account == null) {
            return null;
        }
        Account transferredAccount = new Account();
        transferredAccount.setAccountName(account.getAccountName());
        transferredAccount.setId(account.getId());
        transferredAccount.setStatus(account.getStatus());

        return transferredAccount;
    }
}
