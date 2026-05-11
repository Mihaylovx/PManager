package org.example.dal;

import org.example.domain.Task;
import org.example.entity.TaskEntity;
import org.example.mapper.TaskMapper;
import org.example.repository.ProjectRepository;
import org.example.repository.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TaskDao {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskDao(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Optional<Task> createForProject(Long projectId, Task task) {
        return projectRepository.findById(projectId).map(project -> {
            TaskEntity entity = TaskMapper.toEntity(task);
            entity.setProject(project);
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }

    public boolean deleteFromProject(Long projectId, Long taskId) {
        Optional<TaskEntity> task = taskRepository.findById(taskId);
        if (task.isPresent() && task.get().getProject().getId().equals(projectId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public Optional<Task> updateStatus(Long projectId, Long taskId, boolean completed) {
        Optional<TaskEntity> existing = taskRepository.findById(taskId);
        if (existing.isPresent() && existing.get().getProject().getId().equals(projectId)) {
            TaskEntity entity = existing.get();
            entity.setCompleted(completed);
            return Optional.of(TaskMapper.toDomain(taskRepository.save(entity)));
        }
        return Optional.empty();
    }
}
