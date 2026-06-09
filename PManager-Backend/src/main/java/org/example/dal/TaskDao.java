package org.example.dal;

import org.example.domain.Task;
import org.example.domain.TaskStatus;
import org.example.entity.TaskEntity;
import org.example.mapper.TaskMapper;
import org.example.repository.ProjectRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TaskDao {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskDao(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Optional<Task> createForProject(Long projectId, Task task) {
        return projectRepository.findById(projectId).map(project -> {
            TaskEntity entity = TaskMapper.toEntity(task);
            entity.setProject(project);
            if (task.getAssignedTo() != null) {
                userRepository.findById(task.getAssignedTo().getEmail())
                        .ifPresent(entity::setAssignedTo);
            }
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }

    public Optional<Task> findById(Long taskId) {
        return taskRepository.findById(taskId).map(TaskMapper::toDomain);
    }

    public void deleteById(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Optional<Task> updateStatus(Long taskId, TaskStatus status) {
        return taskRepository.findById(taskId).map(entity -> {
            entity.setStatus(status);
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }

    public Optional<Task> updateHours(Long taskId, Double hoursWorked) {
        return taskRepository.findById(taskId).map(entity -> {
            entity.setHoursWorked(hoursWorked);
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }

    public Optional<Task> updateTask(Long taskId, TaskStatus status, Double hoursWorked, String assignedTo) {
        return taskRepository.findById(taskId).map(entity -> {
            if (status != null) entity.setStatus(status);
            if (hoursWorked != null) entity.setHoursWorked(hoursWorked);
            if (assignedTo != null) {
                userRepository.findById(assignedTo).ifPresent(entity::setAssignedTo);
            }
            return TaskMapper.toDomain(taskRepository.save(entity));
        });
    }
}
