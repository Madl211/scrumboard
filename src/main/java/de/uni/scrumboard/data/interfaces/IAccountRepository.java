package de.uni.scrumboard.data.interfaces;

import java.util.Optional;

import de.uni.scrumboard.data.Account;

import java.util.List;

public interface IAccountRepository {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Optional<Account> findById(int accountId);
    List<Account> getAllAccounts();

    Account createAccount(String email, String passwordHash, String username);

    void updateAccount(Account account);
}
