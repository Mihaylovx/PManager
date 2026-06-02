package org.example.service;

import org.example.DTO.ProjectEventDTO;
import org.example.domain.Task;
import org.example.mapper.TaskMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProjectNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public ProjectNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTaskDeleted(Long projectId, Long taskId) {
        broadcast(projectId, new ProjectEventDTO("TASK_DELETED", taskId));
    }

    public void notifyTaskUpdated(Long projectId, Task task) {
        broadcast(projectId, new ProjectEventDTO("TASK_UPDATED", TaskMapper.toDTO(task)));
    }

    private void broadcast(Long projectId, ProjectEventDTO event) {
        messagingTemplate.convertAndSend("/topic/projects/" + projectId, event);
    }
}