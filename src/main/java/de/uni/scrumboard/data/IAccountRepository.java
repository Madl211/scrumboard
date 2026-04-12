package de.uni.scrumboard.data;

import java.util.Optional;

public interface IAccountRepository {
    Optional<Account> findByEmail(String email);

    Account createAccount(String email, String passwordHash, String username);
}
