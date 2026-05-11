package service;

import org.example.dal.TaskDao;
import org.example.domain.Task;
import org.example.service.TaskServiceImpl;
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

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Write tests")
                .completed(false)
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
        when(taskDao.deleteFromProject(10L, 1L)).thenReturn(true);

        boolean result = taskService.deleteTask(10L, 1L);

        assertTrue(result);
        verify(taskDao, times(1)).deleteFromProject(10L, 1L);
    }

    @Test
    void deleteTask_nonExistingTask_returnsFalse() {
        when(taskDao.deleteFromProject(10L, 99L)).thenReturn(false);

        boolean result = taskService.deleteTask(10L, 99L);

        assertFalse(result);
    }

    @Test
    void updateTaskStatus_existingTask_returnsUpdatedTask() {
        Task completed = Task.builder()
                .id(1L)
                .title("Write tests")
                .completed(true)
                .projectId(10L)
                .build();
        when(taskDao.updateStatus(10L, 1L, true)).thenReturn(Optional.of(completed));

        Optional<Task> result = taskService.updateTaskStatus(10L, 1L, true);

        assertTrue(result.isPresent());
        assertTrue(result.get().isCompleted());
        verify(taskDao, times(1)).updateStatus(10L, 1L, true);
    }

    @Test
    void updateTaskStatus_nonExistingTask_returnsEmpty() {
        when(taskDao.updateStatus(10L, 99L, true)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.updateTaskStatus(10L, 99L, true);

        assertTrue(result.isEmpty());
    }
}
