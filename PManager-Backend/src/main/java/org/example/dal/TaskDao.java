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

    public Optional<Task> findById(Long taskId) {
        return taskRepository.findById(taskId).map(TaskMapper::toDomain);
    }

    public void deleteById(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Optional<Task> updateStatus(Long taskId, boolean completed) {
        return taskRepository.findById(taskId).map(entity -> {
            entity.setCompleted(completed);
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }
}
