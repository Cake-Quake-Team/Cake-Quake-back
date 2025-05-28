package com.cakequake.cakequakeback.security.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("---CustomUserDetailsService---loadUserByUsername---호출---");

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new CustomUserDetails(member);
    }
}
