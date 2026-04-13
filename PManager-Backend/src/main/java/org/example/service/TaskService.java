package org.example.service;

import org.example.domain.Task;
import org.example.entity.TaskEntity;
import org.example.mapper.TaskMapper;
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
        return projectRepository.findById(projectId).map(projectEntity -> {
            TaskEntity taskEntity = TaskMapper.toEntity(task);
            taskEntity.setProject(projectEntity);
            return TaskMapper.toDomain(taskRepository.save(taskEntity));
        });
    }

    public boolean deleteTask(Long projectId, Long taskId) {
        Optional<TaskEntity> task = taskRepository.findById(taskId);
        if (task.isPresent() && task.get().getProject().getId().equals(projectId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public Optional<Task> updateTaskStatus(Long projectId, Long taskId, boolean completed) {
        Optional<org.example.entity.TaskEntity> task = taskRepository.findById(taskId);
        if (task.isPresent() && task.get().getProject().getId().equals(projectId)) {
            TaskEntity existingTask = task.get();
            existingTask.setCompleted(completed);
            return Optional.of(TaskMapper.toDomain(taskRepository.save(existingTask)));
        }
        return Optional.empty();
    }
}