package org.example.service;

import org.example.dal.TaskDao;
import org.example.domain.Task;
import org.example.domain.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final ProjectNotificationService notificationService;

    public TaskServiceImpl(TaskDao taskDao, ProjectNotificationService notificationService) {
        this.taskDao = taskDao;
        this.notificationService = notificationService;
    }

    public Optional<Task> createTask(Long projectId, Task task) {
        return taskDao.createForProject(projectId, task);
    }

    public boolean deleteTask(Long projectId, Long taskId) {
        return taskDao.findById(taskId)
                .filter(t -> projectId.equals(t.getProjectId()))
                .map(t -> {
                    taskDao.deleteById(taskId);
                    notificationService.notifyTaskDeleted(projectId, taskId);
                    return true;
                })
                .orElse(false);
    }

    public Optional<Task> updateTaskStatus(Long projectId, Long taskId, TaskStatus status) {
        Optional<Task> updated = taskDao.findById(taskId)
                .filter(t -> projectId.equals(t.getProjectId()))
                .flatMap(t -> taskDao.updateStatus(taskId, status));
        updated.ifPresent(t -> notificationService.notifyTaskUpdated(projectId, t));
        return updated;
    }

    public Optional<Task> updateTask(Long projectId, Long taskId, TaskStatus status, Double hoursWorked, String assignedTo) {
        Optional<Task> updated = taskDao.findById(taskId)
                .filter(t -> projectId.equals(t.getProjectId()))
                .flatMap(t -> taskDao.updateTask(taskId, status, hoursWorked, assignedTo));
        updated.ifPresent(t -> notificationService.notifyTaskUpdated(projectId, t));
        return updated;
    }
}
