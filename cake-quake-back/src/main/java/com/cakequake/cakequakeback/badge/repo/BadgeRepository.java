package com.cakequake.cakequakeback.badge.repo;

import com.cakequake.cakequakeback.badge.entities.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByBadgeId(Long badgeId);

    Optional<Badge> findByIcon(String icon);
}
