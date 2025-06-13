package com.cakequake.cakequakeback.security.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticatedUserService {

    public Member getCurrentMember() {
        log.debug("---AuthenticatedUserService---getCurrentMember()---");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getMember();
    }

    public Long getCurrentMemberId() {
        log.debug("---AuthenticatedUserService---getCurrentMemberId()---");
        return getCurrentMember().getUid();
    }
}
