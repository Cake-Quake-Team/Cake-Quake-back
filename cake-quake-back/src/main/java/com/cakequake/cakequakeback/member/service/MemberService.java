package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(BuyerSignupRequestDTO requestDTO) {

        if (memberRepository.existsByUserId(requestDTO.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String encodedPassword = null;
        if ("basic".equalsIgnoreCase(requestDTO.getJoinType())) {
            encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        }

        Member member = Member.builder()
                .userId(requestDTO.getUserId())
                .uname(requestDTO.getUname())
                .password(encodedPassword)
                .phoneNumber(requestDTO.getPhoneNumber())
                .publicInfo(Boolean.valueOf(requestDTO.getPublicInfo()))
                .alarm(requestDTO.getAlarm())
                .role(MemberRole.from(requestDTO.getRole()))
                .socialType(SocialType.from(requestDTO.getJoinType()))
                .build();

        memberRepository.save(member);
    }

}
