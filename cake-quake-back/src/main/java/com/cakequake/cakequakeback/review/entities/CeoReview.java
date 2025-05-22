package com.cakequake.cakequakeback.review.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="ceo_reivew")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeoReview {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ceo_review_seq_gen")
    @SequenceGenerator(
            name = "ceo_review_seq_gen",
            sequenceName = "ceo_review_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private long ceoReviewId;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "reviewId" , nullable = false,unique = true)
    private Review review;

    @Column(name = "content", nullable = false, length = 200)
    private String content;


}
