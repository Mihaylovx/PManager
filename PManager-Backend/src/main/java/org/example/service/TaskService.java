package org.example.service;

import org.example.domain.Task;
import org.example.domain.TaskStatus;

import java.util.Optional;

public interface TaskService {
    Optional<Task> createTask(Long projectId, Task task);
    boolean deleteTask(Long projectId, Long taskId);
    Optional<Task> updateTaskStatus(Long projectId, Long taskId, TaskStatus status);
    Optional<Task> updateTask(Long projectId, Long taskId, TaskStatus status, Double hoursWorked, String assignedTo);
}
