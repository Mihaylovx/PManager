package org.example.mapper;

import org.example.domain.Task;
import org.example.domain.TaskStatus;
import org.example.domain.User;
import org.example.DTO.TaskDTO;
import org.example.entity.TaskEntity;

public class TaskMapper {

    public static TaskDTO toDTO(Task domain) {
        return TaskDTO.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .status(domain.getStatus() != null ? domain.getStatus() : TaskStatus.TODO)
                .hoursWorked(domain.getHoursWorked() != null ? domain.getHoursWorked() : 0.0)
                .assignedTo(domain.getAssignedToEmail())
                .build();
    }

    public static Task toDomain(TaskDTO dto) {
        User assignedUser = dto.getAssignedTo() != null
                ? User.builder().email(dto.getAssignedTo()).build()
                : null;
        return Task.builder()
                .title(dto.getTitle())
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO)
                .hoursWorked(dto.getHoursWorked() != null ? dto.getHoursWorked() : 0.0)
                .assignedTo(assignedUser)
                .build();
    }

    public static Task toDomain(TaskEntity entity) {
        return Task.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .status(entity.getStatus() != null ? entity.getStatus() : TaskStatus.TODO)
                .hoursWorked(entity.getHoursWorked() != null ? entity.getHoursWorked() : 0.0)
                .assignedTo(entity.getAssignedTo() != null ? UserMapper.toDomain(entity.getAssignedTo()) : null)
                .projectId(entity.getProject().getId())
                .build();
    }

    public static TaskEntity toEntity(Task domain) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(domain.getTitle());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus() : TaskStatus.TODO);
        entity.setHoursWorked(domain.getHoursWorked() != null ? domain.getHoursWorked() : 0.0);
        // assignedTo (UserEntity FK) is resolved and set by the DAL
        return entity;
    }
}
