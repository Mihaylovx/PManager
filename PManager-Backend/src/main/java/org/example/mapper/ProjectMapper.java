package org.example.mapper;

import org.example.domain.Project;
import org.example.DTO.ProjectDTO;
import org.example.DTO.TaskDTO;
import org.example.entity.ProjectEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMapper {

    private ProjectMapper() {}

    public static ProjectDTO toDTO(Project domain) {
        List<TaskDTO> taskDTOs = domain.getTasks() == null ? List.of() :
                domain.getTasks().stream()
                        .map(TaskMapper::toDTO)
                        .collect(Collectors.toList());

        Set<String> members = domain.getMemberEmails() == null ? Set.of() :
                new HashSet<>(domain.getMemberEmails());

        return ProjectDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .lastUpdated(domain.getLastUpdated())
                .managerEmail(domain.getManagerEmail())
                .memberEmails(members)
                .tasks(taskDTOs)
                .build();
    }

    public static Project toDomain(ProjectDTO dto) {
        Set<String> members = dto.getMemberEmails() == null ? new HashSet<>() :
                new HashSet<>(dto.getMemberEmails());
        return Project.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .lastUpdated(dto.getLastUpdated())
                .managerEmail(dto.getManagerEmail())
                .memberEmails(members)
                .build();
    }

    public static Project toDomain(ProjectEntity entity) {
        return Project.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .lastUpdated(entity.getLastUpdated())
                .managerEmail(entity.getManagerEmail())
                .memberEmails(entity.getMemberEmails() == null ? new HashSet<>() : new HashSet<>(entity.getMemberEmails()))
                .tasks(entity.getTasks().stream()
                        .map(TaskMapper::toDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    public static ProjectEntity toEntity(Project domain) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setManagerEmail(domain.getManagerEmail());
        if (domain.getMemberEmails() != null) {
            entity.setMemberEmails(new HashSet<>(domain.getMemberEmails()));
        }
        return entity;
    }
}
