package org.example.service;

import org.example.domain.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<Project> getAllProjects();
    Optional<Project> getProjectById(Long id);
    Project createProject(Project project);
    Optional<Project> updateProject(Long id, Project projectDetails);
    void deleteProject(Long id);
}