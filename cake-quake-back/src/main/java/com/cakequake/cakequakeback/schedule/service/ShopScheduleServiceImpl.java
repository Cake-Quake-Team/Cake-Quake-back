package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.schedule.repo.ShopScheduleRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopScheduleServiceImpl implements ShopScheduleService {
    private final ShopRepository shopRepository;
    private final ShopScheduleRepository shopScheduleRepository;
    private final MemberRepository memberRepository;








}
