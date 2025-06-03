package com.cakequake.cakequakeback.member.service.seller;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.CustomImageUtils;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;
import com.cakequake.cakequakeback.member.entities.PendingSellerRequest;
import com.cakequake.cakequakeback.member.entities.SellerRequestStatus;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.PendingSellerRequestRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Transactional
@Slf4j
public class SellerServiceImpl implements SellerService{

    private final PendingSellerRequestRepository pendingSellerRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;
    private final CustomImageUtils customImageUtils;

    public SellerServiceImpl(PendingSellerRequestRepository pendingSellerRequestRepository, PasswordEncoder passwordEncoder, MemberValidator memberValidator, CustomImageUtils customImageUtils) {
        this.pendingSellerRequestRepository = pendingSellerRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberValidator = memberValidator;
        this.customImageUtils = customImageUtils;
    }

    @Override
    public ApiResponseDTO registerStepOne(SellerSignupStep1RequestDTO requestDTO) {

        log.info("---registerStepOne---requestDTO: {}", requestDTO.toString());

        SocialType joinType = SocialType.from(requestDTO.getJoinType());
        MultipartFile file = requestDTO.getBusinessCertificate();

        /*
            유효성 형식 검사 - userId, 비밀번호, uname 길이, 전화번호 형식, 가입 방식, 사업자 등록 번호, 대표자 성명, 개업 일자, 매장명
            중복 검사 - userId, 전화번호
        */
        memberValidator.validateSellerSignup(requestDTO);
        log.debug("---registerStepOne---memberValidator 통과---");

        // basic 가입일 때만 비밀번호 인코딩
        String encodedPassword = null;
        if (joinType == SocialType.BASIC) {
            encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        }

        // 휴대폰 인증, 사업자 등록 진위여부 검증은 프론트에서 따로 호출

        /*
            파일 처리 - 사업자 등록증 파일
         */
        String uploadDir = "C:\\nginx-1.26.3\\html\\selleruploads";

        String savedName = customImageUtils.saveImageFile(file, uploadDir);

        PendingSellerRequest pendingSeller = PendingSellerRequest.builder()
                .userId(requestDTO.getUserId())
                .uname(requestDTO.getUname())
                .password(encodedPassword)
                .phoneNumber(requestDTO.getPhoneNumber())
                .businessNumber(requestDTO.getBusinessNumber())
                .bossName(requestDTO.getBossName())
                .openingDate(requestDTO.getOpeningDate())
                .shopName(requestDTO.getShopName())
                .publicInfo(true)   // 프론트에서 동의 받아야 2단계로 진행할 거라서 고정
                .socialType(joinType)
                .businessCertificateUrl(savedName)  // 파일명만 저장
                .status(SellerRequestStatus.PENDING)
                .build();

        pendingSellerRequestRepository.save(pendingSeller);

        return ApiResponseDTO.builder()
                .success(true)
                .message("1단계 저장 성공하였습니다. 다음 매장 정보 입력 단계로 진행해 주세요.")
                .data(pendingSeller.getTemp_seller_id())
                .build();
    }

    @Override
    public ApiResponseDTO registerStepTwo(SellerSignupStep2RequestDTO dto) {

        log.debug("SellerSignupStep2RequestDTO: {}", dto.toString());
        // 가입 2단계 DTO 형식 검사 + 매장 번호 중복 검사
        memberValidator.validateSellerSignup2(dto);
        log.debug("---registerStepTwo---memberValidator 통과---");

        // 1단계에서 저장된 임시 판매자 조회
        PendingSellerRequest pendingSeller = pendingSellerRequestRepository.findById(dto.getTempSellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TEMP_SELLER_ID));    // 1001

        // 이미지 저장 처리
        String shopImageName = null; // 대표 이미지
        String sanitationImageName = null; // 위생 인증서

        String shopImageDir = "C:\\nginx-1.26.3\\html\\shop\\Images";
        String sanitationImageDir = "C:\\nginx-1.26.3\\html\\selleruploads";

        if (dto.getShopImage() != null && !dto.getShopImage().isEmpty()) {
            shopImageName = customImageUtils.saveImageFile(dto.getShopImage(), shopImageDir);
        }

        if (dto.getSanitationCertificate() != null && !dto.getSanitationCertificate().isEmpty()) {
            sanitationImageName = customImageUtils.saveImageFile(dto.getSanitationCertificate(), sanitationImageDir);
        }

        log.debug("shopImageName: {}, sanitationImageName: {}", shopImageName, sanitationImageName);

        // 판매자 정보 업데이트
        pendingSeller.changeAddress(dto.getShopAddress());
        pendingSeller.changeShopPhoneNumber(dto.getShopPhoneNumber());
        pendingSeller.changeOpenTime(dto.getOpenTime());
        pendingSeller.changeCloseTime(dto.getCloseTime());
        pendingSeller.changeMainProductDescription(dto.getMainProductDescription());
        pendingSeller.changeShopImageUrl(shopImageName);
        pendingSeller.changeSanitationCertificateUrl(sanitationImageName);

        pendingSellerRequestRepository.save(pendingSeller);

        return ApiResponseDTO.builder()
                .success(true)
                .message("판매자 승인 요청이 접수되었습니다. 관리자의 승인을 기다려주세요.")
                .build();
    }


}
