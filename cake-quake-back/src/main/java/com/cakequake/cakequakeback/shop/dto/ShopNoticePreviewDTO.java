//매장 정보에서 공지사항 간략히 볼 때
package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder

public class ShopNoticePreviewDTO {
    private Long shopNoticeId;
    private Long shopId;
    private String title;
    private String previewContent;;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

    public ShopNoticePreviewDTO(Long shopNoticeId, Long shopId, String title, String previewContent,
                                LocalDateTime regDate, LocalDateTime modDate) {
        this.shopNoticeId = shopNoticeId;
        this.shopId = shopId;
        this.title = title;
        this.previewContent = previewContent;
        this.regDate = regDate;
        this.modDate = modDate;
    }
}
