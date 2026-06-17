package de.uni.scrumboard.data.enums;

import java.util.NoSuchElementException;

public enum TaskStatus {

    TODO(0),
    IN_PROGRESS(1),
    COMPLETED(2),
    REVIEW(3),
    TEST(4);

    public final int val;

    private TaskStatus(int val) {
        this.val = val;
    }

    public static TaskStatus itoStatus(int statusInt) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.val == statusInt)
                return status;
        }
        throw new NoSuchElementException();
    }
}