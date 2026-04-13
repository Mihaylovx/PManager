package org.example.mapper;

import org.example.domain.Task;
import org.example.DTO.TaskDTO;
import org.example.entity.TaskEntity;

public class TaskMapper {

    public static TaskDTO toDTO(Task domain) {
        return TaskDTO.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .completed(domain.isCompleted())
                .build();
    }

    public static Task toDomain(TaskDTO dto) {
        return Task.builder()
                .title(dto.getTitle())
                .completed(dto.isCompleted())
                .build();
    }

    public static Task toDomain(TaskEntity entity) {
        return Task.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .completed(entity.isCompleted())
                .projectId(entity.getProject().getId())
                .build();
    }

    public static TaskEntity toEntity(Task domain) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(domain.getTitle());
        entity.setCompleted(domain.isCompleted());
        return entity;
    }
}