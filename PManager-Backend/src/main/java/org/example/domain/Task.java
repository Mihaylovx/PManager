package org.example.domain;

import lombok.*;

@Getter
@Builder
public class Task {
    private Long id;
    @Setter
    private String title;
    @Setter
    private TaskStatus status;
    @Setter
    private Double hoursWorked;
    @Setter
    private User assignedTo;
    private Long projectId;

    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }

    public String getAssignedToEmail() {
        return assignedTo != null ? assignedTo.getEmail() : null;
    }
}
