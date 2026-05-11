package org.example.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class ProjectDTO {
    private Long id;

    @Size(max = 100, message = "Project name is too long.")
    @NotBlank(message = "Project name is required.")
    private String name;

    @Size(max = 300, message = "Description is too long.")
    private String description;

    private LocalDateTime lastUpdated;
    private String managerEmail;
    private Set<String> memberEmails;
    private List<TaskDTO> tasks;
}
