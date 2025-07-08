package com.cakequake.cakequakeback.member.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import com.cakequake.cakequakeback.member.entities.*;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.repo.PendingSellerRequestRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopImageRepository;
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
    private final ShopImageRepository shopImageRepository;

    public AdminServiceImpl(PendingSellerRequestRepository pendingSellerRequestRepository, MemberRepository memberRepository, ShopRepository shopRepository, ShopImageRepository shopImageRepository) {
        this.pendingSellerRequestRepository = pendingSellerRequestRepository;
        this.memberRepository = memberRepository;
        this.shopRepository = shopRepository;
        this.shopImageRepository = shopImageRepository;
    }


    @Transactional(readOnly = true)
    @Override
    public InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PendingSellerPageRequestDTO pageRequestDTO) {

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

        // 2. Shop 저장
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
                .build();

        shopRepository.save(shop);

        // 2-1. 대표 이미지 등록
        ShopImage shopImage = ShopImage.builder()
                .shop(shop)
                .shopImageUrl(request.getShopImageUrl())
                .isThumbnail(true)
                .createdBy(request.getUserId())
                .modifiedBy(request.getUserId())
                .build();

        shopImageRepository.save(shopImage);

        // 3. 요청 상태 변경
        request.changeStatus(SellerRequestStatus.APPROVED);

        return ApiResponseDTO.builder()
                .success(true)
                .message("판매자의 가입이 승인되었습니다.")
                .build();
    }

    // 판매자 가입 보류
    @Override
    public ApiResponseDTO holdPendingSellerStatus(Long tempSellerId) {
        if(tempSellerId == null) throw new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID);

        PendingSellerRequest request = pendingSellerRequestRepository.findById(tempSellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID));

        SellerRequestStatus status = request.getStatus();

        // 상태가 대기(PENDING)가 아니면 예외 발생
        if (status != SellerRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_UPDATE);
        }

        request.changeStatus(SellerRequestStatus.HOLD);

        return ApiResponseDTO.builder()
                .success(true)
                .message("판매자 요청이 보류 처리되었습니다.")
                .build();
    }

    // 판매자 가입 거절
    @Override
    public ApiResponseDTO rejectPendingSellerStatus(Long tempSellerId) {
        if(tempSellerId == null) throw new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID);

        PendingSellerRequest request = pendingSellerRequestRepository.findById(tempSellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID));

        SellerRequestStatus status = request.getStatus();

        // 상태가 대기(PENDING)가 아니면 예외 발생
        if (status != SellerRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_UPDATE);
        }

        request.changeStatus(SellerRequestStatus.REJECTED);

        return ApiResponseDTO.builder()
                .success(true)
                .message("판매자 요청이 거절되었습니다.")
                .build();
    }

}
