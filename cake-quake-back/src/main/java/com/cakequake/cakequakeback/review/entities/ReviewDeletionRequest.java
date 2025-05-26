package com.cakequake.cakequakeback.review.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
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
public class ReviewDeletionRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "deletion_request_seq_gen")
    @SequenceGenerator(
            name = "deletion_request_seq_gen",
            sequenceName = "deletion_request _seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long requestId;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reviewId", nullable = false,unique = true)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionRequestStatus status;

    private String reason;

    /** 관리자 승인 **/
    public void approve() {
        if (status != DeletionRequestStatus.PENDING) {
            throw new IllegalStateException("현재 상태에서 승인할 수 없습니다.");
        }
        this.status = DeletionRequestStatus.APPROVED;
    }

    /** 관리자 거절 **/
    public void reject() {
        if (status != DeletionRequestStatus.PENDING) {
            throw new IllegalStateException("현재 상태에서 거절할 수 없습니다.");
        }
        this.status = DeletionRequestStatus.REJECTED;
    }

}
