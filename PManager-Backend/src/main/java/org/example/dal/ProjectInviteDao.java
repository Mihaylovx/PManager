package org.example.dal;

import org.example.DTO.ProjectInviteDTO;
import org.example.domain.InviteStatus;
import org.example.entity.ProjectInviteEntity;
import org.example.repository.ProjectInviteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProjectInviteDao {

    private final ProjectInviteRepository inviteRepository;

    public ProjectInviteDao(ProjectInviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public ProjectInviteDTO create(Long projectId, String projectName, String invitedByEmail, String inviteeEmail) {
        ProjectInviteEntity entity = new ProjectInviteEntity();
        entity.setProjectId(projectId);
        entity.setProjectName(projectName);
        entity.setInvitedByEmail(invitedByEmail);
        entity.setInviteeEmail(inviteeEmail);
        entity.setStatus(InviteStatus.PENDING);
        return toDTO(inviteRepository.save(entity));
    }

    public List<ProjectInviteDTO> findPendingForUser(String email) {
        return inviteRepository.findByInviteeEmailAndStatus(email, InviteStatus.PENDING)
                .stream().map(this::toDTO).toList();
    }

    public Optional<ProjectInviteDTO> updateStatus(Long inviteId, InviteStatus status) {
        return inviteRepository.findById(inviteId).map(entity -> {
            entity.setStatus(status);
            return toDTO(inviteRepository.save(entity));
        });
    }

    public Optional<ProjectInviteEntity> findEntityById(Long inviteId) {
        return inviteRepository.findById(inviteId);
    }

    public boolean hasPendingInvite(Long projectId, String inviteeEmail) {
        return inviteRepository.findByProjectIdAndInviteeEmailAndStatus(projectId, inviteeEmail, InviteStatus.PENDING)
                .isPresent();
    }

    private ProjectInviteDTO toDTO(ProjectInviteEntity entity) {
        return ProjectInviteDTO.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .projectName(entity.getProjectName())
                .invitedByEmail(entity.getInvitedByEmail())
                .inviteeEmail(entity.getInviteeEmail())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
