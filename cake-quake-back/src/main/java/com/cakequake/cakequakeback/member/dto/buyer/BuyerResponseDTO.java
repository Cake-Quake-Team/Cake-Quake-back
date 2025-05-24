package com.cakequake.cakequakeback.member.dto.buyer;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerResponseDTO {
    private Long uid;
    private String uname;
    private String userId;
    private String phoneNumber;
    private MemberRole role;
    private Boolean alarm;
    private String socialId;
    private SocialType socialType;

    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
