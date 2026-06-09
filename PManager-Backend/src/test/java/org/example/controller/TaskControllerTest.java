package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.TaskDTO;
import org.example.config.SecurityConfig;
import org.example.domain.Task;
import org.example.domain.TaskStatus;
import org.example.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private final Task task = Task.builder()
            .id(1L)
            .title("Implement feature")
            .status(TaskStatus.TODO)
            .hoursWorked(0.0)
            .projectId(10L)
            .build();

    @Test
    void addTask_validTask_returns201WithTaskDTO() throws Exception {
        when(taskService.createTask(eq(10L), any(Task.class))).thenReturn(Optional.of(task));

        TaskDTO dto = TaskDTO.builder()
                .title("Implement feature")
                .status(TaskStatus.TODO)
                .hoursWorked(0.0)
                .build();

        mockMvc.perform(post("/api/projects/10/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Implement feature"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void addTask_projectNotFound_returns404() throws Exception {
        when(taskService.createTask(eq(99L), any(Task.class))).thenReturn(Optional.empty());

        TaskDTO dto = TaskDTO.builder()
                .title("Implement feature")
                .build();

        mockMvc.perform(post("/api/projects/99/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTask_blankTitle_returns400() throws Exception {
        TaskDTO dto = TaskDTO.builder()
                .title("")
                .build();

        mockMvc.perform(post("/api/projects/10/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTask_existingTask_returns200() throws Exception {
        when(taskService.deleteTask(10L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/projects/10/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask_nonExistingTask_returns404() throws Exception {
        when(taskService.deleteTask(10L, 99L)).thenReturn(false);

        mockMvc.perform(delete("/api/projects/10/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_existingTask_returns200WithUpdatedTaskDTO() throws Exception {
        Task updated = Task.builder()
                .id(1L)
                .title("Implement feature")
                .status(TaskStatus.IN_PROGRESS)
                .hoursWorked(3.0)
                .projectId(10L)
                .build();
        when(taskService.updateTask(eq(10L), eq(1L), eq(TaskStatus.IN_PROGRESS), eq(3.0), isNull()))
                .thenReturn(Optional.of(updated));

        TaskDTO dto = TaskDTO.builder()
                .status(TaskStatus.IN_PROGRESS)
                .hoursWorked(3.0)
                .build();

        mockMvc.perform(patch("/api/projects/10/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.hoursWorked").value(3.0));
    }

    @Test
    void updateTask_nonExistingTask_returns404() throws Exception {
        when(taskService.updateTask(eq(10L), eq(99L), any(), any(), any())).thenReturn(Optional.empty());

        TaskDTO dto = TaskDTO.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(patch("/api/projects/10/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
