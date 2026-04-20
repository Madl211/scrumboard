package de.uni.scrumboard.service;

import java.util.List;
import java.util.Optional;

import de.uni.scrumboard.data.Account;
import de.uni.scrumboard.data.PasswordUtil;
import de.uni.scrumboard.data.interfaces.IAccountRepository;
import de.uni.scrumboard.service.interfaces.IAccountService;

public record AccountService(IAccountRepository accountRepository) implements IAccountService {

    @Override
    public Optional<Integer> authenticate(String email, String password) {
        Optional<Account> account;
        if (email.contains("@")) {
            account = accountRepository.findByEmail(email);
        } else {
            account = accountRepository.findByUsername(email);
        }

        return account
            .filter(acc -> PasswordUtil.verify(password, acc.passwordHash()))
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

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }
}
