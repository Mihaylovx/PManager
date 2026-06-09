package org.example.controller;

import org.example.DTO.ProjectInviteDTO;
import org.example.config.SecurityConfig;
import org.example.domain.InviteStatus;
import org.example.service.ProjectInviteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectInviteController.class)
@Import(SecurityConfig.class)
class ProjectInviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectInviteService inviteService;

    private final ProjectInviteDTO invite = ProjectInviteDTO.builder()
            .id(1L)
            .projectId(10L)
            .projectName("Test Project")
            .invitedByEmail("manager@example.com")
            .inviteeEmail("invitee@example.com")
            .status(InviteStatus.PENDING)
            .build();

    @Test
    void getPendingInvites_returns200WithList() throws Exception {
        when(inviteService.getPendingInvitesForUser("invitee@example.com")).thenReturn(List.of(invite));

        mockMvc.perform(get("/api/invites").param("user", "invitee@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inviteeEmail").value("invitee@example.com"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void acceptInvite_pendingInvite_returns200WithAcceptedDTO() throws Exception {
        ProjectInviteDTO accepted = ProjectInviteDTO.builder()
                .id(1L)
                .projectId(10L)
                .inviteeEmail("invitee@example.com")
                .status(InviteStatus.ACCEPTED)
                .build();
        when(inviteService.acceptInvite(1L)).thenReturn(Optional.of(accepted));

        mockMvc.perform(post("/api/invites/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void acceptInvite_nonPendingOrNotFound_returns404() throws Exception {
        when(inviteService.acceptInvite(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/invites/99/accept"))
                .andExpect(status().isNotFound());
    }

    @Test
    void declineInvite_pendingInvite_returns200WithDeclinedDTO() throws Exception {
        ProjectInviteDTO declined = ProjectInviteDTO.builder()
                .id(1L)
                .projectId(10L)
                .inviteeEmail("invitee@example.com")
                .status(InviteStatus.DECLINED)
                .build();
        when(inviteService.declineInvite(1L)).thenReturn(Optional.of(declined));

        mockMvc.perform(post("/api/invites/1/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void declineInvite_nonPendingOrNotFound_returns404() throws Exception {
        when(inviteService.declineInvite(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/invites/99/decline"))
                .andExpect(status().isNotFound());
    }
}
