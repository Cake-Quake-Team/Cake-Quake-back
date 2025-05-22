package com.cakequake.cakequakeback.review.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_deletion_request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDeletionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "deletion_request_seq_gen")
    @SequenceGenerator(
            name = "deletion_request_seq_gen",
            sequenceName = "deletion_request _seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long requestId;

    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reviewId", nullable = false,unique = true)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionRequestStatus status;

    private String reason;


    @LastModifiedDate
    @Column(name = " handeleAt")
    private LocalDateTime handeleAt;  //처리 시각

}
