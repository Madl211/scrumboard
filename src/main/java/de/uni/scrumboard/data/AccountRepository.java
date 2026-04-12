package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public record AccountRepository(Connection connection) implements IAccountRepository {

    @Override
    public Optional<Account> findByEmail(String email) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "SELECT accountId, email, passwordHash, username FROM accounts WHERE email = ?")) {
            sqlStatement.setString(1, email);

            try (ResultSet result = sqlStatement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapResultToAccount(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Laden des Accounts mit der E-Mail " + email, e);
        }
    }

    @Override
    public Account createAccount(String email, String passwordHash, String username) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "INSERT INTO accounts(email, passwordHash, username) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            sqlStatement.setString(1, email);
            sqlStatement.setString(2, passwordHash);
            sqlStatement.setString(3, username);

            int affectedRows = sqlStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Konnte Account nicht anlegen");
            }

            try (ResultSet generatedKeys = sqlStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int accountId = generatedKeys.getInt(1);
                    return new Account(accountId, email, passwordHash, username);
                }
            }

            throw new RuntimeException("Konnte generierte Account-ID nicht lesen");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Anlegen des Accounts", e);
        }
    }

    private Account mapResultToAccount(ResultSet result) throws SQLException {
        int accountId = result.getInt("accountId");
        String email = result.getString("email");
        String passwordHash = result.getString("passwordHash");
        String username = result.getString("username");
        return new Account(accountId, email, passwordHash, username);
    }
}
