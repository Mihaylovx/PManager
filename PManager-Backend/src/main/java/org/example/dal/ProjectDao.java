package org.example.dal;

import org.example.domain.Project;
import org.example.entity.ProjectEntity;
import org.example.mapper.ProjectMapper;
import org.example.repository.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectDao {

    private final ProjectRepository projectRepository;

    public ProjectDao(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::toDomain)
                .toList();
    }

    public List<Project> findAllForUser(String email) {
        return projectRepository.findAll().stream()
                .filter(p -> email.equals(p.getManagerEmail())
                        || (p.getMemberEmails() != null && p.getMemberEmails().contains(email)))
                .map(ProjectMapper::toDomain)
                .toList();
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id)
                .map(ProjectMapper::toDomain);
    }

    public Project save(Project project) {
        ProjectEntity saved = projectRepository.save(ProjectMapper.toEntity(project));
        return ProjectMapper.toDomain(saved);
    }

    public Optional<Project> update(Long id, Project details) {
        return projectRepository.findById(id).map(entity -> {
            entity.setName(details.getName());
            entity.setDescription(details.getDescription());
            if (details.getMemberEmails() != null) {
                entity.setMemberEmails(new HashSet<>(details.getMemberEmails()));
            }
            return ProjectMapper.toDomain(projectRepository.save(entity));
        });
    }

    public Optional<Project> addMember(Long id, String email) {
        return projectRepository.findById(id).map(entity -> {
            if (entity.getMemberEmails() == null) {
                entity.setMemberEmails(new HashSet<>());
            }
            entity.getMemberEmails().add(email);
            return ProjectMapper.toDomain(projectRepository.save(entity));
        });
    }

    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }
}
