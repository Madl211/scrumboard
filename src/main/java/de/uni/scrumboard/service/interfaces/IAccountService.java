package de.uni.scrumboard.service.interfaces;

import java.util.Optional;

import java.util.List;

import de.uni.scrumboard.data.Account;

public interface IAccountService {
    Optional<Integer> authenticate(String email, String password);

    Optional<Integer> register(String email, String password, String username);
    
    List<Account> getAllAccounts();
}
