package org.example.service;

import org.example.dal.ProjectDao;
import org.example.domain.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserService userService;

    @Mock
    private ProjectNotificationService notificationService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .managerEmail("manager@example.com")
                .build();
    }

    @Test
    void getAllProjects_returnsListOfProjects() {
        when(projectDao.findAll()).thenReturn(List.of(project));

        List<Project> result = projectService.getAllProjects();

        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(projectDao, times(1)).findAll();
    }

    @Test
    void getAllProjects_emptyList_returnsEmptyList() {
        when(projectDao.findAll()).thenReturn(List.of());

        List<Project> result = projectService.getAllProjects();

        assertTrue(result.isEmpty());
    }

    @Test
    void getProjectsForUser_asManager_returnsProject() {
        when(projectDao.findAll()).thenReturn(List.of(project));

        List<Project> result = projectService.getProjectsForUser("manager@example.com");

        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    void getProjectsForUser_asMember_returnsProject() {
        Project projectWithMember = Project.builder()
                .id(2L)
                .name("Member Project")
                .memberEmails(new HashSet<>(Set.of("member@example.com")))
                .build();
        when(projectDao.findAll()).thenReturn(List.of(projectWithMember));

        List<Project> result = projectService.getProjectsForUser("member@example.com");

        assertEquals(1, result.size());
        assertEquals("Member Project", result.get(0).getName());
    }

    @Test
    void getProjectsForUser_notRelated_returnsEmpty() {
        when(projectDao.findAll()).thenReturn(List.of(project));

        List<Project> result = projectService.getProjectsForUser("stranger@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void createProject_noMembers_returnsSavedProject() {
        when(projectDao.save(any(Project.class))).thenReturn(project);

        Project result = projectService.createProject(project);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectDao, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_withValidMembers_returnsSavedProject() {
        Project withMembers = Project.builder()
                .name("Team Project")
                .memberEmails(new HashSet<>(Set.of("alice@example.com", "bob@example.com")))
                .build();
        when(userService.existsByEmail("alice@example.com")).thenReturn(true);
        when(userService.existsByEmail("bob@example.com")).thenReturn(true);
        when(projectDao.save(any(Project.class))).thenReturn(withMembers);

        Project result = projectService.createProject(withMembers);

        assertNotNull(result);
        verify(projectDao, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_withInvalidMember_throwsIllegalArgumentException() {
        Project withBadMember = Project.builder()
                .name("Team Project")
                .memberEmails(new HashSet<>(Set.of("ghost@example.com")))
                .build();
        when(userService.existsByEmail("ghost@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(withBadMember));
        verify(projectDao, never()).save(any());
    }

    @Test
    void deleteProject_existingId_callsDeleteById() {
        projectService.deleteProject(1L);

        verify(projectDao, times(1)).deleteById(1L);
    }

    @Test
    void getProjectById_existingId_returnsProject() {
        when(projectDao.findById(1L)).thenReturn(Optional.of(project));

        Optional<Project> result = projectService.getProjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Project", result.get().getName());
    }

    @Test
    void getProjectById_nonExistingId_returnsEmpty() {
        when(projectDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.getProjectById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProject_existingId_returnsUpdatedProject() {
        Project updated = Project.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .build();
        when(projectDao.update(eq(1L), any(Project.class))).thenReturn(Optional.of(updated));

        Optional<Project> result = projectService.updateProject(1L, updated);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
    }

    @Test
    void updateProject_nonExistingId_returnsEmpty() {
        when(projectDao.update(eq(99L), any(Project.class))).thenReturn(Optional.empty());

        Optional<Project> result = projectService.updateProject(99L, project);

        assertTrue(result.isEmpty());
    }

    @Test
    void inviteMember_existingUser_returnsUpdatedProject() {
        when(userService.existsByEmail("new@example.com")).thenReturn(true);
        when(projectDao.addMember(1L, "new@example.com")).thenReturn(Optional.of(project));

        Optional<Project> result = projectService.inviteMember(1L, "new@example.com");

        assertTrue(result.isPresent());
        verify(projectDao, times(1)).addMember(1L, "new@example.com");
    }

    @Test
    void inviteMember_nonExistentUser_throwsIllegalArgumentException() {
        when(userService.existsByEmail("ghost@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> projectService.inviteMember(1L, "ghost@example.com"));
        verify(projectDao, never()).addMember(any(), any());
    }
}
