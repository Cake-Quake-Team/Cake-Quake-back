package com.cakequake.cakequakeback.point.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO;
import com.cakequake.cakequakeback.point.dto.PointResponseDTO;
import com.cakequake.cakequakeback.point.entities.ChangeType;
import com.cakequake.cakequakeback.point.entities.Point;
import com.cakequake.cakequakeback.point.entities.PointHistory;
import com.cakequake.cakequakeback.point.repo.PointHistoryRepo;
import com.cakequake.cakequakeback.point.repo.PointRepo;
import com.cakequake.cakequakeback.point.validator.PointValidator;
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

    private final PointRepo pointRepo;
    private final PointHistoryRepo pointHistoryRepo;
    private final PointValidator validator;


    //특정 사용자의 현재 포인트 잔액을 조회함
    @Override
    @Transactional(readOnly = true)
    public Long getCurrentBalance(Long uid) {

        // 회원 검증 및 Point 엔티티 확보
        var member = validator.validateMemberExists(uid);
        var point = validator.getOrCreatePoint(member);
        return point.getTotalPoints();
    }

    //특정 사용자의 포인트르르 증감 처리 합니다.
    @Override
    public Long changePoint(Long uid, Long amount, String description) {
        // 회원 검증 및 Point 엔티티 확보
        var member = validator.validateMemberExists(uid);
        var point = validator.getOrCreatePoint(member);

        Long beforePoint = point.getTotalPoints();
        Long afterPoint = beforePoint + amount;


        if (afterPoint < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS);
        }
        //Point 엔티팉의 totalPoints를 갱신하고 저장
        point.updateTotalPoints(afterPoint);
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
        // 회원 검증
        var member = validator.validateMemberExists(uid);
        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<PointHistoryResponseDTO> dtoPage = pointHistoryRepo.findDtoByMemberOrderByRegDateDesc(member, pageable);


        return InfiniteScrollResponseDTO.<PointHistoryResponseDTO>builder()
                .content(dtoPage.getContent())
                .hasNext(dtoPage.hasNext())
                .totalCount((int) dtoPage.getTotalElements())
                .build();
    }
}
