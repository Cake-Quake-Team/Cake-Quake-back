package com.cakequake.cakequakeback.review.entities;

import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.java.ShortPrimitiveArrayJavaType;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "review_seq_gen")
    @SequenceGenerator(
            name = "review_seq_gen",
            sequenceName = "seq_gen",
            initialValue = 1,
            allocationSize = 5
    )
    @Column(name = "reviewId")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private Member user;

    //주문테이블과 연결
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "orderId",nullable = false)
//    private Order order;

//    //매장테이블과 연결
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "shopId", nullable = false)
//    private Shop shop;


//    케이크(상품)테이블과 연결
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cakeId", nullable = false)
//    private Cake cake;

    //0~5까지의 값을 한정
    @Min(0)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    //ERD에 null가능인데 불가능으로 바꾸고 추가함
    @Column(length = 50, nullable=false)
    private String content;

    @Column(name = "reviewPictureUrl", length = 255)
    private String reviewPictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;


    @OneToOne(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true,  //
            fetch = FetchType.LAZY,
            optional = true)
    private CeoReview ceoReview;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ReviewDeletionRequest deletionRequest;
}