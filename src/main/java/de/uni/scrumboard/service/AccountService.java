package de.uni.scrumboard.service;

import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.IAccountRepository;
import de.uni.scrumboard.data.PasswordUtil;

public record AccountService(IAccountRepository accountRepository) implements IAccountService {

    @Override
    public Optional<Integer> authenticate(String email, String password) {
        return accountRepository.findByEmail(email)
                .filter(account -> PasswordUtil.verify(password, account.passwordHash()))
                .map(Account::accountId);
    }

    @Override
    public Optional<Integer> register(String email, String password, String username) {
        if (accountRepository.findByEmail(email).isPresent()) {
            return Optional.empty();
        }

        String passwordHash = PasswordUtil.hash(password);
        Account account = accountRepository.createAccount(email, passwordHash, username);
        return Optional.of(account.accountId());
    }
}
