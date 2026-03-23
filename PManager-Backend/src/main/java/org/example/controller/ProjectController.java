package org.example.controller;

import org.example.entity.Project;
import org.example.entity.Task;
import org.example.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.getProjectById(id);
        if (project.isPresent()) {
            return ResponseEntity.ok(project.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        Optional<Project> updatedProject = projectService.updateProject(id, projectDetails);
        return updatedProject.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Fix the addTask method to properly handle the response
    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> addTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            Optional<Task> savedTask = projectService.addTask(id, task);
            if (savedTask.isPresent()) {
                // Return only the task, not the whole project with circular references
                Task responseTask = savedTask.get();
                return ResponseEntity.status(HttpStatus.CREATED).body(responseTask);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding task: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @PathVariable Long taskId) {
        boolean deleted = projectService.deleteTask(id, taskId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @PathVariable Long taskId, @RequestBody Task taskUpdate) {
        Optional<Task> updatedTask = projectService.updateTaskStatus(id, taskId, taskUpdate.isCompleted());
        return updatedTask.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}