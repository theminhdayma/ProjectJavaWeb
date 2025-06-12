package com.data.service.account;

import com.data.entity.Account;
import com.data.repository.account.AccountRep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImp implements AccountService {

    private final AccountRep accountRep;

    @Override
    public boolean login(String email, String password) {
        return accountRep.login(email, password);
    }

    @Override
    public boolean register(Account account) {
        return accountRep.register(account);
    }

    @Override
    public boolean checkEmail(String email) {
        return accountRep.checkEmail(email);
    }

    @Override
    public Account findAccountByEmail(String email) {
        return accountRep.findAccountByEmail(email);
    }
}
