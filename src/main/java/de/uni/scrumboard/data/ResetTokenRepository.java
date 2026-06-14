package de.uni.scrumboard.data;

import java.util.Optional;

public interface ResetTokenRepository {
    void save(PasswordResetToken token);         
    Optional<PasswordResetToken> findByToken(String token);  
    void deleteByAccountId(Long accountId);
    void update(PasswordResetToken token);         
}