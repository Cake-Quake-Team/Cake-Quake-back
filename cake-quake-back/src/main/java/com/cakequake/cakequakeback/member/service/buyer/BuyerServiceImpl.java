package com.cakequake.cakequakeback.member.service.buyer;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.AlarmSettingsDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerModifyDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class BuyerServiceImpl implements BuyerService{

    private final MemberRepository memberRepository;

    private final AuthenticatedUserService authenticatedUserService;
    private final MemberValidator memberValidator;

    public BuyerServiceImpl(MemberRepository memberRepository, AuthenticatedUserService authenticatedUserService, MemberValidator memberValidator) {
        this.memberRepository = memberRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.memberValidator = memberValidator;
    }

    // 프로필 조회
    @Override
    public ApiResponseDTO getBuyerProfile(Long uid) {
        // uid가 없는 경우
        if(uid == null) throw new BusinessException(ErrorCode.NOT_FOUND_UID);

        String currentUserId = authenticatedUserService.getCurrentMember().getUserId();

        BuyerProfileResponseDTO buyerDTO = memberRepository.buyerGetOne(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!buyerDTO.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        BuyerProfileResponseDTO responseDTO = BuyerProfileResponseDTO.builder()
                .uid(buyerDTO.getUid())
                .userId(buyerDTO.getUserId())
                .uname(buyerDTO.getUname())
                .phoneNumber(buyerDTO.getPhoneNumber())
                .alarm(buyerDTO.getAlarm())
                .build();

        return ApiResponseDTO.builder()
                .success(true)
                .message("구매자 프로필 조회 성공")
                .data(responseDTO)
                .build();
    }

    // 구매자 프로필 수정
    @Override
    public ApiResponseDTO modifyBuyerProfile(Long uid, BuyerModifyDTO modifyDTO) {
        // url로 넘어온 uid
        if(uid == null) throw new BusinessException(ErrorCode.NOT_FOUND_UID);

        String currentUserId = authenticatedUserService.getCurrentMember().getUserId();

        Member buyer = memberRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 로그인한 유저ID와 조회한 userId가 다를 때
        if (!buyer.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        // 전화번호가 기존과 다를 때만 전화번호 형식 + 중복 검사
        if (!buyer.getPhoneNumber().equals(modifyDTO.getPhoneNumber())) {
            memberValidator.validatePhoneNumber(modifyDTO.getPhoneNumber());
        }

        // 휴대폰 인증은 프론트에서 따로 호출

        buyer.changeUname(modifyDTO.getUname());
        buyer.changePhoneNumber(modifyDTO.getPhoneNumber());

        memberRepository.save(buyer);

        return ApiResponseDTO.builder()
                .success(true)
                .message("구매자 프로필 수정 성공")
                .build();
    }

    // 알람 설정
    @Override
    public ApiResponseDTO modifyBuyerAlarm(Long uid, AlarmSettingsDTO alarmSettingsDTO) {
        // url로 넘어온 uid
        if(uid == null) throw new BusinessException(ErrorCode.NOT_FOUND_UID);

        String currentUserId = authenticatedUserService.getCurrentMember().getUserId();

        Member buyer = memberRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 로그인한 유저ID와 조회한 userId가 다를 때
        if (!buyer.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        // 알람 설정 업데이트
        buyer.changeAlarm(alarmSettingsDTO);

        memberRepository.save(buyer);

        return ApiResponseDTO.builder()
                .success(true)
                .message("알람 설정 변경 성공")
                .build();
    }

    @Override
    public ApiResponseDTO withdrawBuyer() {
        Member member = authenticatedUserService.getCurrentMember(); // ACTIVE 유저만 나옴

        member.withdraw(); // Repository에서 탈퇴 status 세팅(status ACTIVE -> WITHDRAWN)
        memberRepository.save(member); // 반영

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        return ApiResponseDTO.builder()
                .success(true)
                .message("탈퇴가 완료되었습니다.")
                .build();
    }
}
