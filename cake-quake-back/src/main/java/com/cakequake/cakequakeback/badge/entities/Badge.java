package com.cakequake.cakequakeback.badge.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges") // 뱃지 정보 테이블
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId; // 뱃지 고유 ID (PK)

    @Column(nullable = false, unique = true, length = 100)
    private String name; // 뱃지 이름 (예: "황금 비율 반죽")

    @Column(nullable = false)
    private String icon; // 뱃지 이미지 URL

    @Column(columnDefinition = "TEXT")
    private String description; // 뱃지 설명
}
