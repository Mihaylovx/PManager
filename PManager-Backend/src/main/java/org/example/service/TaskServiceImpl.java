package org.example.service;

import org.example.dal.TaskDao;
import org.example.domain.Task;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;

    public TaskServiceImpl(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public Optional<Task> createTask(Long projectId, Task task) {
        return taskDao.createForProject(projectId, task);
    }

    public boolean deleteTask(Long projectId, Long taskId) {
        return taskDao.findById(taskId)
                .filter(t -> projectId.equals(t.getProjectId()))
                .map(t -> { taskDao.deleteById(taskId); return true; })
                .orElse(false);
    }

    public Optional<Task> updateTaskStatus(Long projectId, Long taskId, boolean completed) {
        return taskDao.findById(taskId)
                .filter(t -> projectId.equals(t.getProjectId()))
                .flatMap(t -> taskDao.updateStatus(taskId, completed));
    }
}
