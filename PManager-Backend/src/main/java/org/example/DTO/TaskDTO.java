package org.example.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.domain.TaskStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;

    @NotBlank(message = "Task title is required.")
    private String title;

    private TaskStatus status;
    private Double hoursWorked;
    private String assignedTo;
}
