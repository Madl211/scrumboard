package de.uni.scrumboard.data;

import java.util.Date;
import java.util.List;

import de.uni.scrumboard.data.enums.TaskStatus;

public record Task(int taskId, String title, String desc, List<Account> assignees, Account creator, Date start,
                Date due,
                Date created, int estimatedTime, int actualTime, TaskStatus status) {

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

}
