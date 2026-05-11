package org.example.service;

import org.example.dal.ProjectDao;
import org.example.domain.Project;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDao projectDao;
    private final UserService userService;

    public ProjectServiceImpl(ProjectDao projectDao, UserService userService) {
        this.projectDao = projectDao;
        this.userService = userService;
    }

    public List<Project> getAllProjects() {
        return projectDao.findAll();
    }

    public List<Project> getProjectsForUser(String email) {
        return projectDao.findAll().stream()
                .filter(p -> email.equals(p.getManagerEmail())
                        || p.getMemberEmails().contains(email))
                .toList();
    }

    public Project createProject(Project project) {
        if (project.getMemberEmails() != null && !project.getMemberEmails().isEmpty()) {
            List<String> invalid = project.getMemberEmails().stream()
                    .filter(email -> !userService.existsByEmail(email))
                    .collect(Collectors.toList());
            if (!invalid.isEmpty()) {
                throw new IllegalArgumentException(
                        "No registered users found for: " + String.join(", ", invalid));
            }
        }
        return projectDao.save(project);
    }

    public void deleteProject(Long id) {
        projectDao.deleteById(id);
    }

    public Optional<Project> getProjectById(Long id) {
        return projectDao.findById(id);
    }

    public Optional<Project> updateProject(Long id, Project details) {
        return projectDao.update(id, details);
    }

    public Optional<Project> inviteMember(Long id, String email) {
        if (!userService.existsByEmail(email)) {
            throw new IllegalArgumentException("No registered user found with email: " + email);
        }
        return projectDao.addMember(id, email);
    }
}
