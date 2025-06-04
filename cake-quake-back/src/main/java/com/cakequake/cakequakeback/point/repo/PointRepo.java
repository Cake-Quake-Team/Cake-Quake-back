package com.cakequake.cakequakeback.point.repo;

import com.cakequake.cakequakeback.point.entities.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepo extends JpaRepository<Point, Long> {

    Optional<Point> findByMemberUid(Long uid);
}
