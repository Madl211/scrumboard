package de.uni.scrumboard.data;

import java.util.Optional;

public interface ResetTokenRepository {
    void save(ResetToken token);
    Optional<ResetToken> findByToken(String token);
    void deleteByAccountId(Long accountId);
}