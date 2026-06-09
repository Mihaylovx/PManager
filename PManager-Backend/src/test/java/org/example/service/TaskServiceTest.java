package org.example.service;

import org.example.dal.TaskDao;
import org.example.domain.Task;
import org.example.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskDao taskDao;

    @Mock
    private ProjectNotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Write tests")
                .status(TaskStatus.TODO)
                .hoursWorked(0.0)
                .projectId(10L)
                .build();
    }

    @Test
    void createTask_existingProject_returnsTask() {
        when(taskDao.createForProject(eq(10L), any(Task.class))).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.createTask(10L, task);

        assertTrue(result.isPresent());
        assertEquals("Write tests", result.get().getTitle());
        verify(taskDao, times(1)).createForProject(eq(10L), any(Task.class));
    }

    @Test
    void createTask_nonExistingProject_returnsEmpty() {
        when(taskDao.createForProject(eq(99L), any(Task.class))).thenReturn(Optional.empty());

        Optional<Task> result = taskService.createTask(99L, task);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteTask_existingTask_returnsTrue() {
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));

        boolean result = taskService.deleteTask(10L, 1L);

        assertTrue(result);
        verify(taskDao, times(1)).findById(1L);
        verify(taskDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_nonExistingTask_returnsFalse() {
        when(taskDao.findById(99L)).thenReturn(Optional.empty());

        boolean result = taskService.deleteTask(10L, 99L);

        assertFalse(result);
        verify(taskDao, never()).deleteById(any());
    }

    @Test
    void deleteTask_wrongProject_returnsFalse() {
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));

        boolean result = taskService.deleteTask(99L, 1L);

        assertFalse(result);
        verify(taskDao, never()).deleteById(any());
    }

    @Test
    void updateTaskStatus_existingTask_returnsUpdatedTask() {
        Task done = Task.builder()
                .id(1L)
                .title("Write tests")
                .status(TaskStatus.DONE)
                .hoursWorked(0.0)
                .projectId(10L)
                .build();
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));
        when(taskDao.updateStatus(1L, TaskStatus.DONE)).thenReturn(Optional.of(done));

        Optional<Task> result = taskService.updateTaskStatus(10L, 1L, TaskStatus.DONE);

        assertTrue(result.isPresent());
        assertTrue(result.get().isCompleted());
        verify(taskDao, times(1)).updateStatus(1L, TaskStatus.DONE);
    }

    @Test
    void updateTaskStatus_nonExistingTask_returnsEmpty() {
        when(taskDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.updateTaskStatus(10L, 99L, TaskStatus.DONE);

        assertTrue(result.isEmpty());
        verify(taskDao, never()).updateStatus(any(), any());
    }

    @Test
    void updateTaskStatus_wrongProject_returnsEmpty() {
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.updateTaskStatus(99L, 1L, TaskStatus.DONE);

        assertTrue(result.isEmpty());
        verify(taskDao, never()).updateStatus(any(), any());
    }

    @Test
    void updateTask_existingTask_returnsUpdatedTask() {
        Task updated = Task.builder()
                .id(1L)
                .title("Write tests")
                .status(TaskStatus.IN_PROGRESS)
                .hoursWorked(3.0)
                .projectId(10L)
                .build();
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));
        when(taskDao.updateTask(1L, TaskStatus.IN_PROGRESS, 3.0, "user@example.com")).thenReturn(Optional.of(updated));

        Optional<Task> result = taskService.updateTask(10L, 1L, TaskStatus.IN_PROGRESS, 3.0, "user@example.com");

        assertTrue(result.isPresent());
        assertEquals(TaskStatus.IN_PROGRESS, result.get().getStatus());
        assertEquals(3.0, result.get().getHoursWorked());
        verify(taskDao, times(1)).updateTask(1L, TaskStatus.IN_PROGRESS, 3.0, "user@example.com");
    }

    @Test
    void updateTask_nonExistingTask_returnsEmpty() {
        when(taskDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.updateTask(10L, 99L, TaskStatus.IN_PROGRESS, 1.0, null);

        assertTrue(result.isEmpty());
        verify(taskDao, never()).updateTask(any(), any(), any(), any());
    }

    @Test
    void updateTask_wrongProject_returnsEmpty() {
        when(taskDao.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.updateTask(99L, 1L, TaskStatus.IN_PROGRESS, 1.0, null);

        assertTrue(result.isEmpty());
        verify(taskDao, never()).updateTask(any(), any(), any(), any());
    }
}
