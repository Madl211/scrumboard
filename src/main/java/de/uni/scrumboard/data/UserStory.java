package de.uni.scrumboard.data;

import de.uni.scrumboard.data.enums.TaskStatus;

public record UserStory(int id, String titel, String beschreibung, int creatorId, int backlogId, TaskStatus status, int priorities, long lastUpdated) {
}
