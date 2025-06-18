package com.data.repository.account;

import com.data.entity.Account;

public interface AccountRep {
    Account findByEmail(String email);
    boolean validatePassword(String rawPassword, String storedPassword);
    void updateLoginStatus(String email, boolean status);
    boolean register(Account account);
    boolean checkEmail(String email);
    Account findAccountByEmail(String email);
    boolean resetPassword(String email, String newPassword);
    boolean lockCandidate(int id);
    boolean unlockCandidate(int id);
    Account findAccountByCandidateId(int candidateId);
    boolean changePassword(Account account, String newPassword);
}
