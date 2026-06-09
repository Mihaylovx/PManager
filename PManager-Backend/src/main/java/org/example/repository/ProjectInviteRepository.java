package org.example.repository;

import org.example.domain.InviteStatus;
import org.example.entity.ProjectInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectInviteRepository extends JpaRepository<ProjectInviteEntity, Long> {
    List<ProjectInviteEntity> findByInviteeEmailAndStatus(String inviteeEmail, InviteStatus status);
    Optional<ProjectInviteEntity> findByProjectIdAndInviteeEmailAndStatus(Long projectId, String inviteeEmail, InviteStatus status);
}
