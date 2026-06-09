package org.example.service;

import org.example.DTO.ProjectInviteDTO;

import java.util.List;
import java.util.Optional;

public interface ProjectInviteService {
    ProjectInviteDTO createInvite(Long projectId, String invitedByEmail, String inviteeEmail);
    List<ProjectInviteDTO> getPendingInvitesForUser(String email);
    Optional<ProjectInviteDTO> acceptInvite(Long inviteId);
    Optional<ProjectInviteDTO> declineInvite(Long inviteId);
}
