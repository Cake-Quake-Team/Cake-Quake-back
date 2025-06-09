package com.cakequake.cakequakeback.point.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO;
import com.cakequake.cakequakeback.point.dto.PointResponseDTO;
import com.cakequake.cakequakeback.point.entities.ChangeType;
import com.cakequake.cakequakeback.point.entities.Point;
import com.cakequake.cakequakeback.point.entities.PointHistory;
import com.cakequake.cakequakeback.point.repo.PointHistoryRepo;
import com.cakequake.cakequakeback.point.repo.PointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final MemberRepository memberRepository;
    private final PointRepo pointRepo;
    private final PointHistoryRepo pointHistoryRepo;


    //특정 사용자의 현재 포인트 잔액을 조회함
    @Override
    public Long getCurrentBalance(Long uid) {

        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Point point = pointRepo.findByMemberUid(uid)
                .orElseGet(()->{
                    Point p = new Point();
                    p.setMember(member);
                    p.setTotalPoints(0L);
                    return pointRepo.save(p);
                });


        return point.getTotalPoints();
    }

    //특정 사용자의 포인트르르 증감 처리 합니다.
    @Override
    public Long changePoint(Long uid, Long amount, String description) {
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //Point 엔티티 조회 또는 새로 생성
        Point point =pointRepo.findByMemberUid(uid)
                .orElseGet(() -> {
                    Point p = new Point();
                    p.setMember(member);
                    p.setTotalPoints(0L);
                    return pointRepo.save(p);
                });

        Long beforePoint = point.getTotalPoints();

        // ─── 디버깅용 로그 ───────────────────────────────
        System.out.println("▶ changePoint 호출 직전 beforeBalance = " + beforePoint);
        System.out.println("▶ 요청으로 넘어온 amount = " + amount);
        // ───────────────────────────────────────────────



        Long afterPoint = beforePoint + amount;

        // ─── 디버깅용 로그 ───────────────────────────────
        System.out.println("▶ 계산된 afterBalance = " + afterPoint);
        // ───────────────────────────────────────────────

        if(afterPoint < 0){
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }

        //Point 엔티팉의 totalPoints를 갱신하고 저장
        point.setTotalPoints(afterPoint);
        pointRepo.save(point);

        ChangeType changeType = (amount>=0) ? ChangeType.EARN : ChangeType.USE;

        PointHistory history = PointHistory.builder()
                .member(member)
                .changeType(changeType)
                .description(description)
                .amount(Math.abs(amount))
                .balanceAmount(afterPoint)
                .build();

        pointHistoryRepo.save(history);

        return afterPoint ;
    }


    @Override
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<PointHistoryResponseDTO> getPointHistoryPage(PageRequestDTO pageRequestDTO, Long uid) {
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<PointHistoryResponseDTO> dtoPage = pointHistoryRepo.findDtoByMemberOrderByRegDateDesc(member, pageable);


        return InfiniteScrollResponseDTO.<PointHistoryResponseDTO>builder()
                .content(dtoPage.getContent())
                .hasNext(dtoPage.hasNext())
                .totalCount((int) dtoPage.getTotalElements())
                .build();
    }
}
