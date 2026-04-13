package de.uni.scrumboard.data;

public record Account(int accountId, String email, String passwordHash, String username) {
}
