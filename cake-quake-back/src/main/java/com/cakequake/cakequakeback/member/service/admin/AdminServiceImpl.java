package com.cakequake.cakequakeback.member.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import com.cakequake.cakequakeback.member.entities.*;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.repo.PendingSellerRequestRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AdminServiceImpl implements AdminService{

    private final PendingSellerRequestRepository pendingSellerRequestRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;

    public AdminServiceImpl(PendingSellerRequestRepository pendingSellerRequestRepository, MemberRepository memberRepository, ShopRepository shopRepository) {
        this.pendingSellerRequestRepository = pendingSellerRequestRepository;
        this.memberRepository = memberRepository;
        this.shopRepository = shopRepository;
    }


    @Transactional(readOnly = true)
    @Override
    public InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PageRequestDTO pageRequestDTO) {

        InfiniteScrollResponseDTO<PendingSellerRequestListDTO> dto = pendingSellerRequestRepository.pendingSellerRequestList(pageRequestDTO);

        if (dto != null) {
            return dto;
        } else {
            throw new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID);
        }

    }

    // 판매자 승인
    @Override
    public ApiResponseDTO approvePendingSeller(Long tempSellerId) {

        PendingSellerRequest request = pendingSellerRequestRepository.findById(tempSellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID));

        // 1. Member 저장
        Member member = Member.builder()
                .uname(request.getUname())
                .userId(request.getUserId())
                .password(request.getPassword()) // 인코딩은 가입 시
                .role(MemberRole.SELLER)
                .phoneNumber(request.getPhoneNumber())
                .socialType(request.getSocialType())
                .alarm(true)
                .publicInfo(request.getPublicInfo())
                .status(MemberStatus.ACTIVE)
                .build();

        memberRepository.save(member); // uid 생성됨

        // 2. Shop 저장 (uid 참조) -> 위도 경도 나중에 추가
        Shop shop = Shop.builder()
                .member(member)
                .businessNumber(request.getBusinessNumber())
                .shopName(request.getShopName())
                .address(request.getAddress())
                .bossName(request.getBossName())
                .phone(request.getShopPhoneNumber())
                .content(request.getMainProductDescription())
                .reviewCount(0) // 디폴트
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(ShopStatus.ACTIVE)
//                .lat()
//                .lng()
                .build();

        shopRepository.save(shop);

        // 3. 요청 상태 변경
        request.changeStatus(SellerRequestStatus.APPROVED);

        return ApiResponseDTO.builder()
                .success(true)
                .message("판매자의 가입이 승인되었습니다.")
                .build();
    }

    // 판매자 가입 거절

}
