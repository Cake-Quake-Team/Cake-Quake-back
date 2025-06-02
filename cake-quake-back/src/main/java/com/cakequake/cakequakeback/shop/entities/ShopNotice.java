package com.cakequake.cakequakeback.shop.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table( name = "shop_notice" )
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShopNotice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long shopNoticeId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop;

    @Column(nullable = false)
    @NotBlank(message="title을 입력해주세요.")
    private String title;

    @Column(nullable = false,columnDefinition = "TEXT")
    @NotBlank(message="content를 입력해주세요.")
    private String content;



    //공지사항 수정용
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }


}
