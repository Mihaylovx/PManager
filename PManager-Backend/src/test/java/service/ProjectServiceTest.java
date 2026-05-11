package service;

import org.example.dal.ProjectDao;
import org.example.domain.Project;
import org.example.service.ProjectServiceImpl;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
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
        verify(projectDao, times(1)).findAll();
    }

    @Test
    void createProject_validProject_returnsSavedProject() {
        when(projectDao.save(any(Project.class))).thenReturn(project);

        Project result = projectService.createProject(project);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(projectDao, times(1)).save(any(Project.class));
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
        verify(projectDao, times(1)).findById(1L);
    }

    @Test
    void getProjectById_nonExistingId_returnsEmpty() {
        when(projectDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.getProjectById(99L);

        assertTrue(result.isEmpty());
        verify(projectDao, times(1)).findById(99L);
    }

    @Test
    void updateProject_existingId_returnsUpdatedProject() {
        Project updatedDetails = Project.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        Project updatedProject = Project.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .build();

        when(projectDao.update(eq(1L), any(Project.class))).thenReturn(Optional.of(updatedProject));

        Optional<Project> result = projectService.updateProject(1L, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("Updated Description", result.get().getDescription());
        verify(projectDao, times(1)).update(eq(1L), any(Project.class));
    }

    @Test
    void updateProject_nonExistingId_returnsEmpty() {
        when(projectDao.update(eq(99L), any(Project.class))).thenReturn(Optional.empty());

        Optional<Project> result = projectService.updateProject(99L, project);

        assertTrue(result.isEmpty());
        verify(projectDao, times(1)).update(eq(99L), any(Project.class));
    }
}
