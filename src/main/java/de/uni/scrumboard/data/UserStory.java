package de.uni.scrumboard.data;

public record UserStory(int id, String titel, String beschreibung, int creatorId, int backlogId) {
}
