package com.data.repository.account;

import com.data.entity.Account;

public interface AccountRep {
    boolean login (String email, String password);
    boolean register(Account account);
    boolean checkEmail(String email);
    Account findAccountByEmail(String email);
}
