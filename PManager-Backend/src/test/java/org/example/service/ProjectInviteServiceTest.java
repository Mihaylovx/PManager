package org.example.service;

import org.example.DTO.ProjectInviteDTO;
import org.example.dal.ProjectDao;
import org.example.dal.ProjectInviteDao;
import org.example.domain.InviteStatus;
import org.example.domain.Project;
import org.example.entity.ProjectInviteEntity;
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
public class ProjectInviteServiceTest {

    @Mock
    private ProjectInviteDao inviteDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectInviteServiceImpl projectInviteService;

    private Project project;
    private ProjectInviteDTO inviteDTO;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .managerEmail("manager@example.com")
                .memberEmails(new HashSet<>())
                .build();

        inviteDTO = ProjectInviteDTO.builder()
                .id(10L)
                .projectId(1L)
                .projectName("Test Project")
                .invitedByEmail("manager@example.com")
                .inviteeEmail("invitee@example.com")
                .status(InviteStatus.PENDING)
                .build();
    }

    @Test
    void createInvite_validInvite_returnsDTO() {
        when(userService.existsByEmail("invitee@example.com")).thenReturn(true);
        when(projectDao.findById(1L)).thenReturn(Optional.of(project));
        when(inviteDao.hasPendingInvite(1L, "invitee@example.com")).thenReturn(false);
        when(inviteDao.create(1L, "Test Project", "manager@example.com", "invitee@example.com")).thenReturn(inviteDTO);

        ProjectInviteDTO result = projectInviteService.createInvite(1L, "manager@example.com", "invitee@example.com");

        assertNotNull(result);
        assertEquals("invitee@example.com", result.getInviteeEmail());
        assertEquals(InviteStatus.PENDING, result.getStatus());
    }

    @Test
    void createInvite_nonExistentUser_throwsIllegalArgumentException() {
        when(userService.existsByEmail("ghost@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.createInvite(1L, "manager@example.com", "ghost@example.com"));
        verify(inviteDao, never()).create(any(), any(), any(), any());
    }

    @Test
    void createInvite_projectNotFound_throwsIllegalArgumentException() {
        when(userService.existsByEmail("invitee@example.com")).thenReturn(true);
        when(projectDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.createInvite(99L, "manager@example.com", "invitee@example.com"));
        verify(inviteDao, never()).create(any(), any(), any(), any());
    }

    @Test
    void createInvite_alreadyMember_throwsIllegalArgumentException() {
        Project projectWithMember = Project.builder()
                .id(1L)
                .name("Test Project")
                .managerEmail("manager@example.com")
                .memberEmails(new HashSet<>(Set.of("invitee@example.com")))
                .build();
        when(userService.existsByEmail("invitee@example.com")).thenReturn(true);
        when(projectDao.findById(1L)).thenReturn(Optional.of(projectWithMember));

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.createInvite(1L, "manager@example.com", "invitee@example.com"));
        verify(inviteDao, never()).create(any(), any(), any(), any());
    }

    @Test
    void createInvite_alreadyManager_throwsIllegalArgumentException() {
        when(userService.existsByEmail("manager@example.com")).thenReturn(true);
        when(projectDao.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.createInvite(1L, "manager@example.com", "manager@example.com"));
        verify(inviteDao, never()).create(any(), any(), any(), any());
    }

    @Test
    void createInvite_pendingInviteExists_throwsIllegalArgumentException() {
        when(userService.existsByEmail("invitee@example.com")).thenReturn(true);
        when(projectDao.findById(1L)).thenReturn(Optional.of(project));
        when(inviteDao.hasPendingInvite(1L, "invitee@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.createInvite(1L, "manager@example.com", "invitee@example.com"));
        verify(inviteDao, never()).create(any(), any(), any(), any());
    }

    @Test
    void getPendingInvitesForUser_returnsList() {
        when(inviteDao.findPendingForUser("invitee@example.com")).thenReturn(List.of(inviteDTO));

        List<ProjectInviteDTO> result = projectInviteService.getPendingInvitesForUser("invitee@example.com");

        assertEquals(1, result.size());
        assertEquals("invitee@example.com", result.get(0).getInviteeEmail());
    }

    @Test
    void acceptInvite_pendingInvite_addsToProjectAndReturnsAccepted() {
        ProjectInviteEntity entity = new ProjectInviteEntity();
        entity.setProjectId(1L);
        entity.setInviteeEmail("invitee@example.com");
        entity.setStatus(InviteStatus.PENDING);

        ProjectInviteDTO accepted = ProjectInviteDTO.builder()
                .id(10L)
                .projectId(1L)
                .inviteeEmail("invitee@example.com")
                .status(InviteStatus.ACCEPTED)
                .build();

        when(inviteDao.findEntityById(10L)).thenReturn(Optional.of(entity));
        when(inviteDao.updateStatus(10L, InviteStatus.ACCEPTED)).thenReturn(Optional.of(accepted));

        Optional<ProjectInviteDTO> result = projectInviteService.acceptInvite(10L);

        assertTrue(result.isPresent());
        assertEquals(InviteStatus.ACCEPTED, result.get().getStatus());
        verify(projectDao, times(1)).addMember(1L, "invitee@example.com");
    }

    @Test
    void acceptInvite_nonPendingInvite_returnsEmpty() {
        ProjectInviteEntity entity = new ProjectInviteEntity();
        entity.setProjectId(1L);
        entity.setInviteeEmail("invitee@example.com");
        entity.setStatus(InviteStatus.ACCEPTED);

        when(inviteDao.findEntityById(10L)).thenReturn(Optional.of(entity));

        Optional<ProjectInviteDTO> result = projectInviteService.acceptInvite(10L);

        assertTrue(result.isEmpty());
        verify(projectDao, never()).addMember(any(), any());
        verify(inviteDao, never()).updateStatus(any(), any());
    }

    @Test
    void acceptInvite_nonExistentInvite_returnsEmpty() {
        when(inviteDao.findEntityById(99L)).thenReturn(Optional.empty());

        Optional<ProjectInviteDTO> result = projectInviteService.acceptInvite(99L);

        assertTrue(result.isEmpty());
        verify(projectDao, never()).addMember(any(), any());
    }

    @Test
    void declineInvite_pendingInvite_returnsDeclined() {
        ProjectInviteEntity entity = new ProjectInviteEntity();
        entity.setProjectId(1L);
        entity.setInviteeEmail("invitee@example.com");
        entity.setStatus(InviteStatus.PENDING);

        ProjectInviteDTO declined = ProjectInviteDTO.builder()
                .id(10L)
                .projectId(1L)
                .inviteeEmail("invitee@example.com")
                .status(InviteStatus.DECLINED)
                .build();

        when(inviteDao.findEntityById(10L)).thenReturn(Optional.of(entity));
        when(inviteDao.updateStatus(10L, InviteStatus.DECLINED)).thenReturn(Optional.of(declined));

        Optional<ProjectInviteDTO> result = projectInviteService.declineInvite(10L);

        assertTrue(result.isPresent());
        assertEquals(InviteStatus.DECLINED, result.get().getStatus());
        verify(projectDao, never()).addMember(any(), any());
    }

    @Test
    void declineInvite_nonPendingInvite_returnsEmpty() {
        ProjectInviteEntity entity = new ProjectInviteEntity();
        entity.setProjectId(1L);
        entity.setInviteeEmail("invitee@example.com");
        entity.setStatus(InviteStatus.DECLINED);

        when(inviteDao.findEntityById(10L)).thenReturn(Optional.of(entity));

        Optional<ProjectInviteDTO> result = projectInviteService.declineInvite(10L);

        assertTrue(result.isEmpty());
        verify(inviteDao, never()).updateStatus(any(), any());
    }
}
