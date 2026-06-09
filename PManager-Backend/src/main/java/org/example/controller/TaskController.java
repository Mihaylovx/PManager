package org.example.controller;

import jakarta.validation.Valid;
import org.example.DTO.TaskDTO;
import org.example.mapper.TaskMapper;
import org.example.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/projects/{id}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> addTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        try {
            Optional<TaskDTO> savedTask = taskService.createTask(id, TaskMapper.toDomain(taskDTO))
                    .map(TaskMapper::toDTO);
            if (savedTask.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(savedTask.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding task: " + e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @PathVariable Long taskId) {
        boolean deleted = taskService.deleteTask(id, taskId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(id, taskId, taskDTO.getStatus(), taskDTO.getHoursWorked(), taskDTO.getAssignedTo())
                .map(TaskMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
