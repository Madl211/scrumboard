package de.uni.scrumboard.data;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class ResetTokenRepositoryImpl implements ResetTokenRepository {
    
    private final Connection connection;
    
    public ResetTokenRepositoryImpl(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void save(PasswordResetToken token) {
        String sql = "INSERT INTO password_reset_tokens (account_id, token, expiry_date, used) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, token.getAccountId());
            stmt.setString(2, token.getToken());
            stmt.setString(3, token.getExpiryDate().toString());
            stmt.setBoolean(4, token.isUsed());
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                token.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Speichern des Reset-Tokens", e);
        }
    }
    
    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        String sql = "SELECT id, account_id, token, expiry_date, used, created_at FROM password_reset_tokens WHERE token = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                PasswordResetToken resetToken = new PasswordResetToken();
                resetToken.setId(rs.getLong("id"));
                resetToken.setAccountId(rs.getLong("account_id"));
                resetToken.setToken(rs.getString("token"));
                resetToken.setExpiryDate(LocalDateTime.parse(rs.getString("expiry_date")));
                resetToken.setUsed(rs.getBoolean("used"));
                resetToken.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
                return Optional.of(resetToken);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Suchen des Reset-Tokens", e);
        }
        return Optional.empty();
    }
    
    @Override
    public void deleteByAccountId(Long accountId) {
        String sql = "DELETE FROM password_reset_tokens WHERE account_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Löschen alter Reset-Tokens", e);
        }
    }
    
    @Override
    public void update(PasswordResetToken token) {
        String sql = "UPDATE password_reset_tokens SET used = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, token.isUsed());
            stmt.setLong(2, token.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Aktualisieren des Reset-Tokens", e);
        }
    }
}