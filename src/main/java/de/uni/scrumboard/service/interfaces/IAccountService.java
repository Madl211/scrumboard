package de.uni.scrumboard.service.interfaces;

import java.util.Optional;

public interface IAccountService {
    Optional<Integer> authenticate(String email, String password);

    Optional<Integer> register(String email, String password, String username);
}
