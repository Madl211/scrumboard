package de.uni.scrumboard.data;

import java.util.Date;
import java.util.List;

import de.uni.scrumboard.data.enums.TaskStatus;

public record Task(int taskId, String title, String desc, List<Account> assignees, Account creator, Date start,
        Date due,
        Date created, int estimatedTime, int actualTime, TaskStatus status, int priorities, long lastUpdated) {

    public int priorities() {
        return priorities;
    }

    public int taskId() {
        return taskId;
    }

    public String title() {
        return title;
    }

    public String desc() {
        return desc;
    }

    public List<Account> assignees() {
        return assignees;
    }

    public Account creator() {
        return creator;
    }

    public Date start() {
        return start;
    }

    public Date due() {
        return due;
    }

    public Date created() {
        return created;
    }

    public int estimatedTime() {
        return estimatedTime;
    }

    public int actualTime() {
        return actualTime;
    }

    public TaskStatus status() {
        return status;
    }

    public long lastUpdated() {
        return lastUpdated;
    }

    public boolean isPriorityLow() {
        return priorities == 0;
    }

    public boolean isPriorityMedium() {
        return priorities == 1;
    }

    public boolean isPriorityHigh() {
        return priorities == 2;
    }

    public String getPriorityCount() {
        return switch (priorities) {
            case 0 -> "Niedrig";
            case 1 -> "Mittel";
            case 2 -> "Hoch";
            default -> "Niedrig";
        };
    }

    public String getPriorityColor() {
        return switch (priorities) {
            case 0 -> "#28a745";
            case 1 -> "#fd7e14";
            case 2 -> "#dc3545";
            default -> "#6c757d";
        };
    }
}
