package com.data.service.account;


import com.data.entity.Account;

public interface AccountService {
    boolean login (String email, String password);
    boolean register(Account account);
    boolean checkEmail(String email);
    Account findAccountByEmail(String email);
    boolean resetPassword(String email, String newPassword);
    boolean lockCandidate(int id);
    boolean unlockCandidate(int id);
    Account findAccountByCandidateId(int candidateId);
}
