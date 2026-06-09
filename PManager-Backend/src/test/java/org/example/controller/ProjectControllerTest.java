package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.ProjectDTO;
import org.example.config.SecurityConfig;
import org.example.domain.*;
import org.example.service.ProjectInviteService;
import org.example.service.ProjectService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectInviteService projectInviteService;

    @MockBean
    private UserService userService;

    private final Project project = Project.builder()
            .id(1L)
            .name("Test Project")
            .description("Test Description")
            .managerEmail("manager@example.com")
            .memberEmails(new HashSet<>())
            .tasks(List.of())
            .build();

    @Test
    void getAllProjects_returns200WithList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    void getAllProjects_withUserFilter_returns200WithFilteredList() throws Exception {
        when(projectService.getProjectsForUser("manager@example.com")).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects").param("user", "manager@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].managerEmail").value("manager@example.com"));
    }

    @Test
    void getProjectById_existingId_returns200WithProject() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void getProjectById_nonExistingId_returns404() throws Exception {
        when(projectService.getProjectById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProject_validProject_returns201WithProject() throws Exception {
        when(projectService.createProject(any(Project.class))).thenReturn(project);

        ProjectDTO dto = ProjectDTO.builder()
                .name("Test Project")
                .description("Test Description")
                .managerEmail("manager@example.com")
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void createProject_blankName_returns400() throws Exception {
        ProjectDTO dto = ProjectDTO.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_invalidMembers_returns400() throws Exception {
        when(projectService.createProject(any(Project.class)))
                .thenThrow(new IllegalArgumentException("No registered users found for: ghost@example.com"));

        ProjectDTO dto = ProjectDTO.builder()
                .name("Test Project")
                .memberEmails(Set.of("ghost@example.com"))
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProject_returns200() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProject_existingId_returns200WithUpdatedProject() throws Exception {
        Project updated = Project.builder()
                .id(1L)
                .name("Updated Project")
                .tasks(List.of())
                .build();
        when(projectService.updateProject(eq(1L), any(Project.class))).thenReturn(Optional.of(updated));

        ProjectDTO dto = ProjectDTO.builder()
                .name("Updated Project")
                .build();

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));
    }

    @Test
    void updateProject_nonExistingId_returns404() throws Exception {
        when(projectService.updateProject(eq(99L), any(Project.class))).thenReturn(Optional.empty());

        ProjectDTO dto = ProjectDTO.builder().name("Updated Project").build();

        mockMvc.perform(put("/api/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void inviteMember_validEmail_returns201() throws Exception {
        when(projectInviteService.createInvite(eq(1L), any(), eq("invitee@example.com")))
                .thenReturn(null);

        mockMvc.perform(post("/api/projects/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@example.com\",\"invitedBy\":\"manager@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void inviteMember_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/projects/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProjectSalary_existingProject_returns200WithSalaryData() throws Exception {
        User worker = User.builder()
                .email("worker@example.com")
                .firstName("Worker")
                .lastName("One")
                .hourlyRate(30.0)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Feature")
                .status(TaskStatus.DONE)
                .hoursWorked(8.0)
                .assignedTo(worker)
                .projectId(1L)
                .build();
        Project projectWithData = Project.builder()
                .id(1L)
                .name("Test Project")
                .memberEmails(new HashSet<>(Set.of("worker@example.com")))
                .tasks(List.of(task))
                .build();

        when(projectService.getProjectById(1L)).thenReturn(Optional.of(projectWithData));
        when(userService.findByEmail("worker@example.com")).thenReturn(Optional.of(worker));

        mockMvc.perform(get("/api/projects/1/salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("worker@example.com"))
                .andExpect(jsonPath("$[0].salary").value(240.0));
    }

    @Test
    void getProjectSalary_nonExistingProject_returns404() throws Exception {
        when(projectService.getProjectById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/99/salary"))
                .andExpect(status().isNotFound());
    }
}
