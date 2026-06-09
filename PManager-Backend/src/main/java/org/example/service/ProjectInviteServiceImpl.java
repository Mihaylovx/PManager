package org.example.service;

import org.example.DTO.ProjectInviteDTO;
import org.example.dal.ProjectDao;
import org.example.dal.ProjectInviteDao;
import org.example.domain.InviteStatus;
import org.example.domain.Project;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectInviteServiceImpl implements ProjectInviteService {

    private final ProjectInviteDao inviteDao;
    private final ProjectDao projectDao;
    private final UserService userService;

    public ProjectInviteServiceImpl(ProjectInviteDao inviteDao, ProjectDao projectDao, UserService userService) {
        this.inviteDao = inviteDao;
        this.projectDao = projectDao;
        this.userService = userService;
    }

    @Override
    public ProjectInviteDTO createInvite(Long projectId, String invitedByEmail, String inviteeEmail) {
        if (!userService.existsByEmail(inviteeEmail)) {
            throw new IllegalArgumentException("No registered user found with email: " + inviteeEmail);
        }
        Project project = projectDao.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
        if (project.getMemberEmails().contains(inviteeEmail) || inviteeEmail.equals(project.getManagerEmail())) {
            throw new IllegalArgumentException("User is already a member of this project.");
        }
        if (inviteDao.hasPendingInvite(projectId, inviteeEmail)) {
            throw new IllegalArgumentException("A pending invite already exists for this user.");
        }
        return inviteDao.create(projectId, project.getName(), invitedByEmail, inviteeEmail);
    }

    @Override
    public List<ProjectInviteDTO> getPendingInvitesForUser(String email) {
        return inviteDao.findPendingForUser(email);
    }

    @Override
    public Optional<ProjectInviteDTO> acceptInvite(Long inviteId) {
        return inviteDao.findEntityById(inviteId).flatMap(entity -> {
            if (entity.getStatus() != InviteStatus.PENDING) {
                return Optional.empty();
            }
            projectDao.addMember(entity.getProjectId(), entity.getInviteeEmail());
            return inviteDao.updateStatus(inviteId, InviteStatus.ACCEPTED);
        });
    }

    @Override
    public Optional<ProjectInviteDTO> declineInvite(Long inviteId) {
        return inviteDao.findEntityById(inviteId).flatMap(entity -> {
            if (entity.getStatus() != InviteStatus.PENDING) {
                return Optional.empty();
            }
            return inviteDao.updateStatus(inviteId, InviteStatus.DECLINED);
        });
    }
}
