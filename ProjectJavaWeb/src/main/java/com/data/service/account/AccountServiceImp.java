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
    public Account findByEmail(String email) {
        return accountRep.findByEmail(email);
    }

    @Override
    public boolean validatePassword(String rawPassword, String storedPassword) {
        return accountRep.validatePassword(rawPassword, storedPassword);
    }

    @Override
    public void updateLoginStatus(String email, boolean status) {
        accountRep.updateLoginStatus(email, status);
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

    @Override
    public boolean resetPassword(String email, String newPassword) {
        return accountRep.resetPassword(email, newPassword);
    }

    @Override
    public boolean lockCandidate(int id) {
        return accountRep.lockCandidate(id);
    }

    @Override
    public boolean unlockCandidate(int id) {
        return accountRep.unlockCandidate(id);
    }

    @Override
    public Account findAccountByCandidateId(int candidateId) {
        return accountRep.findAccountByCandidateId(candidateId);
    }

    @Override
    public boolean changePassword(Account account, String newPassword) {
        return accountRep.changePassword(account, newPassword);
    }
}
