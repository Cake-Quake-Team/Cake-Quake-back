package com.cakequake.cakequakeback.review.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.shop.entities.Shop;
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
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Member user;

    //주문테이블과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId",nullable = false)
    private CakeOrder order;

    //매장테이블과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopId", nullable = false)
    private Shop shop;


    //케이크(상품)테이블과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cakeId", nullable = false)
    private CakeItem cakeItem;

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



    //Setter 추가
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReviewPictureUrl(String reviewPictureUrl) {
        this.reviewPictureUrl = reviewPictureUrl;
    }

    public void setCeoReview(CeoReview ceoReview) {
        this.ceoReview = ceoReview;
    }

    //구매자 삭제 요청  -> DELETE_REQUEST에서는 삭제 요청을 못하도록 막기 관리자 승인 삭제될 경우 uesr온도 지수 하락으로 연장?
    public void deleteByBuyer(){
        if(status != ReviewStatus.ACTIVE){
            throw new IllegalStateException("ACTIVE 상태에서만 구매자 삭제가 가능합니다.");
        }
        this.status = ReviewStatus.DELETED;
    }

    //판매자 삭제 요청
    public void requestDelete(){
        if(status != ReviewStatus.ACTIVE){
            throw new IllegalStateException("삭제 요청은 ACTIVE 상태에서만 가능합니다.");
        }
        this.status = ReviewStatus.DELTE_REQUEST;
    }

    //관리자 승인 (삭제)
    public void softDelete(){
        if(status != ReviewStatus.DELTE_REQUEST){
            throw new IllegalStateException("DELETE_REQUEST 상태에서만 숨김 처리 가능");
        }
        this.status = ReviewStatus.DELETED;
    }

    //관리자 거절 (요청 취소)
    public void cancelDeleteRequest(){
        if(status != ReviewStatus.DELTE_REQUEST){
            throw new IllegalStateException("DELETE_REQUEST 상태에서만 숨김 처리 가능");
        }
        this.status = ReviewStatus.ACTIVE;
    }

}