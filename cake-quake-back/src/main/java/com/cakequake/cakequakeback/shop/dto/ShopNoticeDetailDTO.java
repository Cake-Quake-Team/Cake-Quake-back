//매장별 공지사항 자세히 보기
package com.cakequake.cakequakeback.shop.dto;

public class ShopNoticeDetailDTO {
    private Long shopNoticeId;
    private Long shopId;
    private String title;
    private String content; // 전체 내용
    private boolean isVisible;

    public ShopNoticeDetailDTO(Long shopNoticeId, Long shopId, String title, String content, boolean isVisible) {
        this.shopNoticeId = shopNoticeId;
        this.shopId = shopId;
        this.title = title;
        this.content = content;
        this.isVisible = isVisible;
    }
}
