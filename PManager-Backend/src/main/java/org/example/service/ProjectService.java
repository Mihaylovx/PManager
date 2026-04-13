package org.example.service;

import org.example.domain.Project;
import org.example.entity.ProjectEntity;
import org.example.mapper.ProjectMapper;
import org.example.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Project createProject(Project project) {
        ProjectEntity saved = projectRepository.save(ProjectMapper.toEntity(project));
        return ProjectMapper.toDomain(saved);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(ProjectMapper::toDomain);
    }

    public Optional<Project> updateProject(Long id, Project projectDetails) {
        return projectRepository.findById(id).map(entity -> {
            entity.setName(projectDetails.getName());
            entity.setDescription(projectDetails.getDescription());
            return ProjectMapper.toDomain(projectRepository.save(entity));
        });
    }
}