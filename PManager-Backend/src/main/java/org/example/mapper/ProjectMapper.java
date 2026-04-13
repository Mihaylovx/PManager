package org.example.mapper;

import org.example.domain.Project;
import org.example.DTO.ProjectDTO;
import org.example.DTO.TaskDTO;
import org.example.entity.ProjectEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectDTO toDTO(Project domain) {
        List<TaskDTO> taskDTOs = domain.getTasks() == null ? List.of() :
                domain.getTasks().stream()
                        .map(TaskMapper::toDTO)
                        .collect(Collectors.toList());

        return ProjectDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .lastUpdated(domain.getLastUpdated())
                .tasks(taskDTOs)
                .build();
    }

    public static Project toDomain(ProjectDTO dto) {
        return Project.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .lastUpdated(dto.getLastUpdated())
                .build();
    }

    public static Project toDomain(ProjectEntity entity) {
        return Project.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .lastUpdated(entity.getLastUpdated())
                .tasks(entity.getTasks().stream()
                        .map(TaskMapper::toDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    public static ProjectEntity toEntity(Project domain) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}