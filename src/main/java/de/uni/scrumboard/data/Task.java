package de.uni.scrumboard.data;

import java.util.Date;
import java.util.List;

import de.uni.scrumboard.data.enums.TaskStatus;

public record Task(int taskId, String title, String desc, List<Account> assignees, Account creator, Date start,
                Date due,
                Date created, int estimatedTime, int actualTime, TaskStatus status) {

}
