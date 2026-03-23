package org.example.service;

import org.example.entity.Project;
import org.example.entity.Task;
import org.example.repository.ProjectRepository;
import org.example.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TaskService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public Optional<Task> addTask(Long projectId, Task task) {
        return projectRepository.findById(projectId).map(project -> {
            task.setProject(project);
            return taskRepository.save(task);
        });
    }

    public boolean deleteTask(Long projectId, Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent() && task.get().getProject().getId().equals(projectId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public Optional<Task> updateTaskStatus(Long projectId, Long taskId, boolean completed) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent() && task.get().getProject().getId().equals(projectId)) {
            Task existingTask = task.get();
            existingTask.setCompleted(completed);
            return Optional.of(taskRepository.save(existingTask));
        }
        return Optional.empty();
    }
}
