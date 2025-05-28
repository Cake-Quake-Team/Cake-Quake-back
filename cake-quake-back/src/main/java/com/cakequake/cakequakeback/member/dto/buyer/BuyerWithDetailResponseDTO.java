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
public class BuyerWithDetailResponseDTO {

    // Member fields (from BuyerResponseDTO)
    private Long uid;
    private String uname;
    private String userId;
    private String phoneNumber;
    private MemberRole role;
    private Boolean alarm;
    private String socialId;
    private SocialType socialType;

    // MemberDetail fields
    private String badges;
    private LocalDateTime delDate;

    private LocalDateTime memberRegDate;
    private LocalDateTime memberModDate;
    private LocalDateTime memberDetailModDate;

    @Override
    public String toString() {
        return String.format("BuyerSignupRequestDTO{uname='%s', userId='%s', role='%s', alarm=%s, socialType='%s', badges='%s', delDate='%s', memberRegDate='%s', memberModDate='%s', memberDetailModDate='%s'}",
                uname, userId, role, alarm, socialType, badges, delDate, memberRegDate, memberModDate, memberDetailModDate);
    }

}
