package org.example.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
public class TaskDTO {
    private Long id;

    @NotBlank(message = "Task title is required.")
    private String title;
    private boolean completed;
}