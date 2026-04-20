package service;

import org.example.domain.Project;
import org.example.entity.ProjectEntity;
import org.example.repository.ProjectRepository;
import org.example.service.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private ProjectEntity projectEntity;
    private Project project;

    @BeforeEach
    void setUp() {
        projectEntity = new ProjectEntity();
        projectEntity.setName("Test Project");
        projectEntity.setDescription("Test Description");

        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .build();
    }

    @Test
    void getAllProjects_returnsListOfProjects() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(List.of(projectEntity));

        // Act
        List<Project> result = projectService.getAllProjects();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getAllProjects_emptyList_returnsEmptyList() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(List.of());

        // Act
        List<Project> result = projectService.getAllProjects();

        // Assert
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void createProject_validProject_returnsSavedProject() {
        // Arrange
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(projectEntity);

        // Act
        Project result = projectService.createProject(project);

        // Assert
        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    void deleteProject_existingId_callsDeleteById() {

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void getProjectById_existingId_returnsProject() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectEntity));

        // Act
        Optional<Project> result = projectService.getProjectById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Project", result.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getProjectById_nonExistingId_returnsEmpty() {
        // Arrange
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Project> result = projectService.getProjectById(99L);

        // Assert
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findById(99L);
    }

    @Test
    void updateProject_existingId_returnsUpdatedProject() {
        // Arrange
        Project updatedDetails = Project.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        ProjectEntity updatedEntity = new ProjectEntity();
        updatedEntity.setName("Updated Name");
        updatedEntity.setDescription("Updated Description");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(updatedEntity);

        // Act
        Optional<Project> result = projectService.updateProject(1L, updatedDetails);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("Updated Description", result.get().getDescription());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    void updateProject_nonExistingId_returnsEmpty() {
        // Arrange
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Project> result = projectService.updateProject(99L, project);

        // Assert
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findById(99L);
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }
}