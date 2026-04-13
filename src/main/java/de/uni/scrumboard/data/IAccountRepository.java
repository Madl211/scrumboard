package de.uni.scrumboard.data;

import java.util.Optional;

public interface IAccountRepository {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);

    Account createAccount(String email, String passwordHash, String username);
}
