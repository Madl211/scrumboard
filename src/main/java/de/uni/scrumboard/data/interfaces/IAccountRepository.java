package de.uni.scrumboard.data.interfaces;

import java.util.Optional;

import de.uni.scrumboard.data.Account;

public interface IAccountRepository {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);

    Account createAccount(String email, String passwordHash, String username);
}
