package com.cakequake.cakequakeback.badge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BadgeServiceImpl implements BadgeService {

    @Override
    public void setProfileBadge(Long memberUid, Long badgeId) {

    }
}
