//매장별 공지사항 자세히 보기
package com.cakequake.cakequakeback.shop.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder

public class ShopNoticeDetailDTO {
    private Long shopNoticeId;
    private Long shopId;
    private String title;
    private String content; // 전체 내용
    private boolean isVisible;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

    public ShopNoticeDetailDTO(Long shopNoticeId, Long shopId, String title,
                               String content, boolean isVisible,
                               LocalDateTime regDate, LocalDateTime modDate) {
        this.shopNoticeId = shopNoticeId;
        this.shopId = shopId;
        this.title = title;
        this.content = content;
        this.isVisible = isVisible;
        this.regDate = regDate;
        this.modDate = modDate;
    }
}
