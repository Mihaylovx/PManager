package org.example.controller;

import jakarta.validation.Valid;
import org.example.DTO.ProjectDTO;
import org.example.mapper.ProjectMapper;
import org.example.service.ProjectInviteService;
import org.example.service.ProjectService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectInviteService projectInviteService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, ProjectInviteService projectInviteService, UserService userService) {
        this.projectService = projectService;
        this.projectInviteService = projectInviteService;
        this.userService = userService;
    }

    @GetMapping
    public List<ProjectDTO> getAllProjects(@RequestParam(value = "user", required = false) String userEmail) {
        List<org.example.domain.Project> projects = (userEmail == null || userEmail.isBlank())
                ? projectService.getAllProjects()
                : projectService.getProjectsForUser(userEmail);
        return projects.stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ProjectMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        try {
            ProjectDTO saved = ProjectMapper.toDTO(
                    projectService.createProject(ProjectMapper.toDomain(projectDTO))
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(id, ProjectMapper.toDomain(projectDTO))
                .map(ProjectMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String inviteeEmail = body.get("email");
        String invitedByEmail = body.get("invitedBy");
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(projectInviteService.createInvite(id, invitedByEmail, inviteeEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<?> getProjectSalary(@PathVariable Long id) {
        return projectService.getProjectById(id).map(project -> {
            Map<String, Double> hourlyRates = project.getMemberEmails().stream()
                    .collect(Collectors.toMap(
                            email -> email,
                            email -> userService.findByEmail(email)
                                    .map(u -> u.getHourlyRate() != null ? u.getHourlyRate() : 0.0)
                                    .orElse(0.0)
                    ));
            return ResponseEntity.ok(project.computeSalaries(hourlyRates));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
