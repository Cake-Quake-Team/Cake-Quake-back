package com.cakequake.cakequakeback.member.service.buyer;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerProfileResponseDTO;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;

public class BuyerServiceImpl implements BuyerService{

    private final MemberRepository memberRepository;

    private final AuthenticatedUserService authenticatedUserService;

    public BuyerServiceImpl(MemberRepository memberRepository, AuthenticatedUserService authenticatedUserService) {
        this.memberRepository = memberRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    public ApiResponseDTO getBuyerProfile(Long uid) {
        // uid가 없는 경우
        if(uid == null) throw new BusinessException(ErrorCode.NOT_FOUND_UID);

        String currentUserId = authenticatedUserService.getCurrentMember().getUserId();

        BuyerProfileResponseDTO buyerDTO = memberRepository.buyerGetOne(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!buyerDTO.getRole().equals(MemberRole.SELLER)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER);
        }

        if (!buyerDTO.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER_SELLER);
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
}
